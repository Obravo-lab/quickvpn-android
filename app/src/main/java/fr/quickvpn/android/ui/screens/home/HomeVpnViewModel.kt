package fr.quickvpn.android.ui.screens.home

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.quickvpn.android.core.network.ApiClient
import fr.quickvpn.android.core.network.User
import fr.quickvpn.android.core.security.TokenStore
import fr.quickvpn.android.vpn.VpnManager
import fr.quickvpn.android.vpn.VpnStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val vpnUp: Boolean = false,
    val vpnError: String? = null,
    val stats: VpnStats = VpnStats(),
    val configReady: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val loggedOut: Boolean = false
)

class HomeVpnViewModel(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val appContext: Context
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    private val _pendingConsent = MutableStateFlow<Intent?>(null)
    val pendingConsent: StateFlow<Intent?> = _pendingConsent

    init {
        VpnManager.bind(appContext)
        viewModelScope.launch {
            VpnManager.isUp.collect { up ->
                _ui.update { it.copy(vpnUp = up) }
            }
        }
        viewModelScope.launch {
            VpnManager.error.collect { err ->
                _ui.update { it.copy(vpnError = err) }
            }
        }
        viewModelScope.launch {
            VpnManager.stats.collect { stats ->
                _ui.update { it.copy(stats = stats) }
            }
        }
        refresh()
    }

    fun refresh() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.me()
                val user = resp.data?.user
                if (resp.ok && user != null) {
                    _ui.update { it.copy(loading = false, user = user) }
                    loadConfig()
                } else {
                    _ui.update { it.copy(loading = false, error = resp.error?.message) }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = "network") }
            }
        }
    }

    private suspend fun loadConfig() {
        if (tokenStore.wgConfig != null) {
            _ui.update { it.copy(configReady = true) }
            return
        }
        val configText = fetchConfig()
        if (configText != null) {
            tokenStore.wgConfig = configText
            _ui.update { it.copy(configReady = true) }
        }
    }

    private suspend fun fetchConfig(): String? {
        try {
            val resp = api.service.config()
            if (resp.ok && resp.data != null) return resp.data.config
        } catch (_: Exception) {
        }
        try {
            val resp = api.service.configGenerate()
            if (resp.ok && resp.data != null) return resp.data.config
        } catch (_: Exception) {
        }
        return null
    }

    fun prepareConnect() {
        val existing = tokenStore.wgConfig
        if (existing.isNullOrBlank()) {
            viewModelScope.launch {
                val config = fetchConfig()
                if (config != null) {
                    tokenStore.wgConfig = config
                    _ui.update { it.copy(configReady = true) }
                    requestConsentOrConnect()
                } else {
                    _ui.update { it.copy(error = "vpn_no_config") }
                }
            }
            return
        }
        requestConsentOrConnect()
    }

    private fun requestConsentOrConnect() {
        val consent = VpnService.prepare(appContext)
        if (consent != null) {
            _ui.update { it.copy(info = "vpn_authorize") }
            _pendingConsent.value = consent
        } else {
            _ui.update { it.copy(info = null) }
            connect()
        }
    }

    fun clearPendingConsent() {
        _pendingConsent.value = null
    }

    fun connect() {
        val config = tokenStore.wgConfig ?: return
        doConnect(config)
    }

    private fun doConnect(config: String) {
        _ui.update { it.copy(info = null) }
        VpnManager.connect(appContext, config)
    }

    fun disconnect() {
        VpnManager.disconnect(appContext)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                api.service.logout()
            } catch (_: Exception) {
            }
            api.onLoggedOut()
            VpnManager.stopService(appContext)
            _ui.update { it.copy(loggedOut = true) }
        }
    }
}
