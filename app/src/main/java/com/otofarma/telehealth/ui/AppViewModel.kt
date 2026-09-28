package com.otofarma.telehealth.ui

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otofarma.telehealth.data.Api
import com.otofarma.telehealth.data.ApiException
import com.otofarma.telehealth.data.AssignedApp
import com.otofarma.telehealth.data.Guide
import com.otofarma.telehealth.data.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.os.SystemClock

data class UiState(
    val baseUrl: String,
    val email: String,
    val token: String?,
    val name: String,
    val role: String?,
    val gmailPassword: String?,
    val appPassword: String?,
    val loading: Boolean,
    val passwordBusy: Boolean = false,
    val passwordNote: String? = null,
    val passwordNoteOk: Boolean = false,
    val error: String?,
    val apps: List<AssignedApp>,
    val guide: Guide?,
    val guideTitle: String,
    val busyAppId: String?,
    val installFile: String?,
    val guideLoading: Boolean = false,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SessionStore(application)
    val api = Api(store)
    private var refreshJob: Job? = null
    private var refreshedAt = 0L
    private val guideCache = mutableMapOf<String, Guide>()

    private val _state = MutableStateFlow(
        UiState(
            baseUrl = store.baseUrl,
            email = store.email,
            token = store.token,
            name = store.name,
            role = null,
            gmailPassword = null,
            appPassword = null,
            loading = false,
            error = null,
            apps = emptyList(),
            guide = null,
            guideTitle = "",
            busyAppId = null,
            installFile = null,
        ),
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                withContext(Dispatchers.IO) {
                    val token = api.login(email, password)
                    store.saveSession(token, name = "", email = email.trim())
                    val profile = api.me()
                    store.saveSession(token, profile.name, profile.email)
                    profile
                }.also { profile ->
                    _state.update {
                        it.copy(
                            baseUrl = store.baseUrl,
                            email = profile.email,
                            token = store.token,
                            name = profile.name,
                            role = profile.role,
                            gmailPassword = profile.gmailPassword,
                            appPassword = profile.appPassword,
                        )
                    }
                }
                refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                store.clearSession()
                _state.update { it.copy(loading = false, token = null, error = error.message ?: "ورود انجام نشد") }
            }
        }
    }

    fun refresh(force: Boolean = false) {
        if (store.token.isNullOrBlank()) return
        val fresh = _state.value.apps.isNotEmpty() && SystemClock.elapsedRealtime() - refreshedAt < 12_000
        if (!force && (fresh || refreshJob?.isActive == true)) return
        if (force) refreshJob?.cancel()
        val showSpinner = force || _state.value.apps.isEmpty()
        refreshJob = viewModelScope.launch {
            if (showSpinner) _state.update { it.copy(loading = true, error = null) }
            try {
                val loaded = withContext(Dispatchers.IO) {
                    val profile = api.me()
                    val remote = api.apps()
                    val installed = installedPackages()
                    profile to remote.map { app ->
                        val version = installed[app.packageName]
                        app.copy(installed = installed.containsKey(app.packageName), installedVersion = version)
                    }
                }
                val (profile, merged) = loaded
                store.saveSession(store.token.orEmpty(), profile.name, profile.email)
                refreshedAt = SystemClock.elapsedRealtime()
                _state.update {
                    it.copy(
                        loading = false,
                        apps = merged,
                        error = null,
                        token = store.token,
                        name = profile.name,
                        email = profile.email,
                        role = profile.role,
                        gmailPassword = profile.gmailPassword,
                        appPassword = profile.appPassword,
                    )
                }
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        api.report(merged)
                    } catch (error: ApiException) {
                        if (error.code == 401) logout()
                    } catch (_: Exception) {
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ApiException) {
                if (error.code == 401) logout()
                else _state.update { it.copy(loading = false, error = error.message) }
            } catch (error: Exception) {
                _state.update { it.copy(loading = false, error = error.message ?: "فهرست اپ‌ها خوانده نشد") }
            }
        }
    }

    fun openGuide(app: AssignedApp) {
        if (!app.guidePublished) {
            _state.update {
                it.copy(guide = EmptyGuide, guideTitle = app.name, guideLoading = false, error = null)
            }
            return
        }
        guideCache[app.id]?.let { cached ->
            _state.update { it.copy(guide = cached, guideTitle = app.name, guideLoading = false, error = null) }
            return
        }
        _state.update { it.copy(guideTitle = app.name, guide = null, guideLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val guide = withContext(Dispatchers.IO) { api.guide(app.id) }
                guideCache[app.id] = guide
                _state.update { it.copy(guideLoading = false, guide = guide, guideTitle = app.name) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ApiException) {
                if (error.code == 401) logout()
                else _state.update { it.copy(guideLoading = false, guide = null, error = error.message) }
            } catch (error: Exception) {
                _state.update { it.copy(guideLoading = false, guide = null, error = error.message ?: "راهنما خوانده نشد") }
            }
        }
    }

    fun closeGuide() {
        _state.update { it.copy(guide = null, guideTitle = "", guideLoading = false) }
    }

    fun download(app: AssignedApp) {
        viewModelScope.launch {
            _state.update { it.copy(busyAppId = app.id, error = null) }
            try {
                val file = withContext(Dispatchers.IO) {
                    val directory = File(getApplication<Application>().cacheDir, "apk").apply { mkdirs() }
                    val destination = File(directory, "${app.packageName}.apk")
                    api.downloadApk(app.id, destination)
                    destination
                }
                _state.update { it.copy(busyAppId = null, installFile = file.absolutePath) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(busyAppId = null, error = error.message ?: "دانلود انجام نشد") }
            }
        }
    }

    fun consumeInstall() {
        _state.update { it.copy(installFile = null) }
    }

    fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun savePasswords(gmailPassword: String, appPassword: String) {
        if (store.token.isNullOrBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(passwordBusy = true, passwordNote = null, passwordNoteOk = false) }
            try {
                val profile = withContext(Dispatchers.IO) { api.updatePasswords(gmailPassword, appPassword) }
                _state.update {
                    it.copy(
                        passwordBusy = false,
                        gmailPassword = profile.gmailPassword,
                        appPassword = profile.appPassword,
                        passwordNote = "Passwords saved",
                        passwordNoteOk = true,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        passwordBusy = false,
                        passwordNote = error.message ?: "Could not save the passwords",
                        passwordNoteOk = false,
                    )
                }
            }
        }
    }

    fun logout() {
        refreshJob?.cancel()
        guideCache.clear()
        store.clearSession()
        _state.value = UiState(
            baseUrl = store.baseUrl,
            email = store.email,
            token = null,
            name = "",
            role = null,
            gmailPassword = null,
            appPassword = null,
            loading = false,
            error = null,
            apps = emptyList(),
            guide = null,
            guideTitle = "",
            busyAppId = null,
            installFile = null,
        )
    }

    private companion object {
        val EmptyGuide = Guide(emptyList(), emptyList(), emptyList(), emptyList())
    }

    private fun installedPackages(): Map<String, String?> {
        val manager = getApplication<Application>().packageManager
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            manager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            manager.getInstalledPackages(0)
        }
        return packages.associate { it.packageName to it.versionName }
    }
}
