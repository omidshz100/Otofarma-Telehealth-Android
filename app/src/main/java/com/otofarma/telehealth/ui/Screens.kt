package com.otofarma.telehealth.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.otofarma.telehealth.data.AssignedApp
import com.otofarma.telehealth.data.Guide
import com.otofarma.telehealth.data.GuideBlock
import com.otofarma.telehealth.ui.theme.Canvas
import com.otofarma.telehealth.ui.theme.Ink
import com.otofarma.telehealth.ui.theme.InstalledBg
import com.otofarma.telehealth.ui.theme.InstalledFg
import com.otofarma.telehealth.ui.theme.Line
import com.otofarma.telehealth.ui.theme.MissingBg
import com.otofarma.telehealth.ui.theme.Muted
import com.otofarma.telehealth.ui.theme.Navy
import java.io.File

private enum class Tab { Home, Settings }

@Composable
fun AppRoot(model: AppViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageLoader = remember(model) {
        ImageLoader.Builder(context).okHttpClient(model.api.http).crossfade(true).build()
    }
    var tab by remember { mutableStateOf(Tab.Home) }

    LifecycleResumeEffect(state.token) {
        if (state.token != null) model.refresh()
        onPauseOrDispose { }
    }

    LaunchedEffect(state.installFile) {
        val path = state.installFile ?: return@LaunchedEffect
        try {
            promptInstall(context, File(path))
        } catch (_: ActivityNotFoundException) {
            model.showError("The installer could not be opened")
        } finally {
            model.consumeInstall()
        }
    }

    BackHandler(enabled = state.guide != null) { model.closeGuide() }

    Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when {
                    state.token == null -> LoginScreen(state, model)
                    state.guide != null || state.guideLoading -> GuideScreen(
                        state.guideTitle,
                        state.guide,
                        state.guideLoading,
                        imageLoader,
                        model.api.http,
                        model::closeGuide,
                    )
                    tab == Tab.Settings -> SettingsScreen(state, model)
                    else -> HomeScreen(state, model)
                }
            }
            if (state.token != null && state.guide == null) {
                BottomNav(tab) { tab = it }
            }
        }
    }
}

@Composable
private fun LoginScreen(state: UiState, model: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        Text("Otofarma", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Device Companion", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(Navy),
            contentAlignment = Alignment.Center,
        ) {
            Text("⌁", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome back",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Ink,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Use the email and app password from your administrator.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Muted,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(20.dp))
        Text("Email", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            email,
            { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("you@example.com", color = Muted) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Password", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            password,
            { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your password", color = Muted) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show", color = Navy)
                }
            },
        )
        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { model.login(email, password) },
            enabled = !state.loading && email.isNotBlank() && password.length >= 8,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy),
        ) {
            if (state.loading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Log in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun HomeScreen(state: UiState, model: AppViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Otofarma", color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Your apps", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (state.name.isNotBlank()) Text(state.name, color = Muted, fontSize = 13.sp)
                }
                TextButton(
                    onClick = { model.refresh(force = true) },
                    enabled = !state.loading,
                ) {
                    Text(if (state.loading) "Refreshing" else "Refresh", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            }
            if (state.loading) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = Navy)
            }
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
        if (state.apps.isEmpty() && !state.loading) {
            item { Text("No apps have been assigned yet.", color = Muted) }
        }
        items(state.apps, key = { it.id }) { app ->
            AppCard(app, state.busyAppId, model)
        }
    }
}

@Composable
private fun AppCard(app: AssignedApp, busyAppId: String?, model: AppViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Navy),
                contentAlignment = Alignment.Center,
            ) {
                Text(app.name.take(1).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.name, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (app.installed && !app.installedVersion.isNullOrBlank() && app.installedVersion != app.version) {
                    Text("Update available", color = Muted, fontSize = 12.sp)
                }
            }
            StatusChip(app.installed)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (app.installed) {
                ActionButton("Open", filled = true, modifier = Modifier.weight(1f)) {
                    openInstalledApp(context, app.packageName)
                }
            } else if (app.installMethod == "play" && !app.playUrl.isNullOrBlank()) {
                ActionButton("Google Play", filled = true, modifier = Modifier.weight(1f)) {
                    openPlay(context, app.playUrl)
                }
            } else if (app.installMethod == "file" && app.hasFile) {
                ActionButton(
                    if (busyAppId == app.id) "Downloading" else "Download",
                    filled = true,
                    enabled = busyAppId == null,
                    modifier = Modifier.weight(1f),
                ) { model.download(app) }
            }
            ActionButton("Guide", filled = false, modifier = Modifier.weight(1f)) {
                model.openGuide(app)
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        border = if (filled) null else BorderStroke(1.dp, Navy),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) Navy else Color.White,
            contentColor = if (filled) Color.White else Navy,
            disabledContainerColor = if (filled) Navy.copy(alpha = 0.45f) else Color.White,
            disabledContentColor = if (filled) Color.White else Navy.copy(alpha = 0.45f),
        ),
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun StatusChip(installed: Boolean) {
    val background = if (installed) InstalledBg else MissingBg
    val foreground = if (installed) InstalledFg else Muted
    Text(
        if (installed) "Installed" else "Not installed",
        color = foreground,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun SettingsScreen(state: UiState, model: AppViewModel) {
    val context = LocalContext.current
    val version = remember {
        val info = if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        info.versionName ?: "0.2.1"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Settings", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Account & device preferences", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))
        Text("Account", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(25.dp)).background(Navy),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.name.take(1).uppercase().ifBlank { "U" }, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(state.name.ifBlank { "Device user" }, color = Ink, fontWeight = FontWeight.SemiBold)
                if (!state.role.isNullOrBlank()) Text(state.role, color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Passwords", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        PasswordEditor(state, model)
        Spacer(Modifier.height(22.dp))
        Text("Device", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        SettingsCard(
            listOf(
                "Device information" to Build.MODEL,
                "App version" to version,
            ),
        )
        Spacer(Modifier.height(28.dp))
        TextButton(
            onClick = model::logout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF1F1)),
        ) {
            Text("Log out", color = Color(0xFFB42318), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PasswordEditor(state: UiState, model: AppViewModel) {
    val context = LocalContext.current
    var gmailPassword by remember(state.gmailPassword) { mutableStateOf(state.gmailPassword.orEmpty()) }
    var appPassword by remember(state.appPassword) { mutableStateOf(state.appPassword.orEmpty()) }
    var copied by remember { mutableStateOf<String?>(null) }
    val gmailChanged = gmailPassword != state.gmailPassword.orEmpty()
    val appChanged = appPassword != state.appPassword.orEmpty()
    val canSave = (gmailChanged || appChanged) &&
        (!gmailChanged || gmailPassword.length >= 8) &&
        (!appChanged || appPassword.length >= 8) &&
        !state.passwordBusy

    fun copy(title: String, value: String) {
        if (value.isBlank()) return
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(title, value))
        copied = title
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text("Gmail address", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.email.ifBlank { "—" }, color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            if (state.email.isNotBlank()) {
                TextButton(onClick = { copy("Gmail address", state.email) }) {
                    Text(if (copied == "Gmail address") "Copied" else "Copy", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Gmail password", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            "This is the copy stored for the tablet. Saving it does not change the Google account.",
            color = Muted,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = gmailPassword,
            onValueChange = { gmailPassword = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                TextButton(onClick = { copy("Gmail password", gmailPassword) }, enabled = gmailPassword.isNotBlank()) {
                    Text(if (copied == "Gmail password") "Copied" else "Copy", color = Navy)
                }
            },
        )
        Spacer(Modifier.height(14.dp))
        Text("App password", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("This is the password for signing in to Otofarma.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = appPassword,
            onValueChange = { appPassword = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                TextButton(onClick = { copy("App password", appPassword) }, enabled = appPassword.isNotBlank()) {
                    Text(if (copied == "App password") "Copied" else "Copy", color = Navy)
                }
            },
        )
        state.passwordNote?.let { note ->
            Spacer(Modifier.height(10.dp))
            Text(note, color = if (state.passwordNoteOk) Color(0xFF067647) else MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { model.savePasswords(gmailPassword, appPassword) },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy),
        ) {
            if (state.passwordBusy) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Save passwords", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsCard(rows: List<Pair<String, String>>, copyable: Boolean = false) {
    val context = LocalContext.current
    var copiedTitle by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(16.dp)),
    ) {
        rows.forEach { (title, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(value, color = Muted, fontSize = 12.sp)
                }
                if (copyable && value.isNotBlank() && value != "—") {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                            clipboard.setPrimaryClip(ClipData.newPlainText(title, value))
                            copiedTitle = title
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            if (copiedTitle == title) "Copied" else "Copy",
                            color = Navy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideScreen(
    title: String,
    guide: Guide?,
    loading: Boolean,
    imageLoader: ImageLoader,
    http: okhttp3.OkHttpClient,
    onBack: () -> Unit,
) {
    val sections = listOf(
        "Overview" to guide?.overview.orEmpty(),
        "Setup" to guide?.setup.orEmpty(),
        "Usage" to guide?.usage.orEmpty(),
        "Problems" to guide?.problems.orEmpty(),
    )
    var selected by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("‹", color = Navy, fontSize = 22.sp) }
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Navy),
                contentAlignment = Alignment.Center,
            ) {
                Text(title.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Guide & support", color = Muted, fontSize = 12.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            sections.forEachIndexed { index, (label, _) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextButton(onClick = { selected = index }) {
                        Text(
                            label,
                            color = if (selected == index) Navy else Muted,
                            fontSize = 13.sp,
                            fontWeight = if (selected == index) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                    Box(
                        Modifier
                            .height(3.dp)
                            .fillMaxWidth(0.6f)
                            .background(if (selected == index) Navy else Color.Transparent),
                    )
                }
            }
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Navy)
            }
            sections[selected].second.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing has been published here yet.", color = Muted)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sections[selected].second.size) { index ->
                    val block = sections[selected].second[index]
                    GuideBlockView(
                        index + 1,
                        block,
                        imageLoader,
                        http,
                        showNumber = sections[selected].first != "Overview",
                        showSolution = sections[selected].first == "Problems" || !block.solution.isNullOrBlank(),
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideBlockView(
    number: Int,
    block: GuideBlock,
    imageLoader: ImageLoader,
    http: okhttp3.OkHttpClient,
    showNumber: Boolean,
    showSolution: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showNumber) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFE7F0F8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(number.toString(), color = Navy, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
            }
            Text(block.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        if (block.body.isNotBlank()) Text(block.body, color = Muted, fontSize = 14.sp)
        if (showSolution && !block.solution.isNullOrBlank()) {
            Text(block.solution, color = Navy, fontSize = 14.sp)
        }
        block.images.forEach { image ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(image.url).crossfade(true).build(),
                contentDescription = image.name,
                imageLoader = imageLoader,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit,
            )
        }
        block.videos.forEach { video -> GuideVideo(video.url, http) }
    }
}

@Composable
private fun GuideVideo(url: String, http: okhttp3.OkHttpClient) {
    val context = LocalContext.current
    val player = remember(url) {
        val source = ProgressiveMediaSource.Factory(OkHttpDataSource.Factory(http))
            .createMediaSource(MediaItem.fromUri(url))
        ExoPlayer.Builder(context).build().apply {
            setMediaSource(source)
            prepare()
            playWhenReady = false
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(
        factory = { viewContext -> PlayerView(viewContext).apply { this.player = player } },
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
    )
}

@Composable
private fun BottomNav(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Line)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        NavItem("Home", selected == Tab.Home) { onSelect(Tab.Home) }
        NavItem("Settings", selected == Tab.Settings) { onSelect(Tab.Settings) }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, color = if (selected) Navy else Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

private fun openInstalledApp(context: android.content.Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
    context.startActivity(intent)
}

private fun openPlay(context: android.content.Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun promptInstall(context: android.content.Context, file: File) {
    if (!context.packageManager.canRequestPackageInstalls()) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")),
        )
        return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val matches = if (Build.VERSION.SDK_INT >= 33) {
        context.packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }
    matches.forEach { match ->
        context.grantUriPermission(match.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}
