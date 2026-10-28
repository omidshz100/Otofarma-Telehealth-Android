package com.otofarma.telehealth.kiosk

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Tablet launcher: area, specialty, service, then the page that opens the exam app.
 * The older companion (sign-in and assigned apps) stays in [com.otofarma.telehealth.MainActivity].
 */
class KioskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activity = this@KioskActivity
            SideEffect {
                val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            KioskTheme {
                KioskApp()
            }
        }
    }
}

private sealed interface KioskRoute {
    data object Areas : KioskRoute
    data class Specialties(val areaId: String) : KioskRoute
    data class Services(val areaId: String, val specialtyId: String) : KioskRoute
    data class Open(val serviceId: String) : KioskRoute
}

@Composable
private fun KioskApp() {
    val context = LocalContext.current
    var stack by remember { mutableStateOf(listOf<KioskRoute>(KioskRoute.Areas)) }
    fun push(route: KioskRoute) {
        stack = stack + route
    }
    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    BackHandler(enabled = stack.size > 1) { pop() }

    when (val route = stack.last()) {
        KioskRoute.Areas -> AreaScreen(KioskCatalog.areas) { area ->
            push(KioskRoute.Specialties(area.id))
        }
        is KioskRoute.Specialties -> {
            val area = KioskCatalog.area(route.areaId)
            SpecialtyScreen(
                area = area,
                onBack = ::pop,
                onPick = { specialty ->
                    push(KioskRoute.Services(area.id, specialty.id))
                },
            )
        }
        is KioskRoute.Services -> {
            val specialty = KioskCatalog.specialty(route.areaId, route.specialtyId)
            ServiceScreen(
                specialty = specialty,
                onBack = ::pop,
                onPick = { service -> push(KioskRoute.Open(service.id)) },
            )
        }
        is KioskRoute.Open -> OpenScreen(
            service = KioskCatalog.service(route.serviceId),
            onBack = ::pop,
            onOpen = { launchExam(context, KioskCatalog.service(route.serviceId).packageName) },
        )
    }
}

private fun launchExam(context: android.content.Context, packageName: String?): Boolean {
    if (packageName.isNullOrBlank()) return false
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
