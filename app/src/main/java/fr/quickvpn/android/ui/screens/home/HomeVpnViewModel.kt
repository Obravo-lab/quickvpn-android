package fr.quickvpn.android.ui.screens.home

import android.content.Context
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
    val loggedOut: Boolean = false
)

class HomeVpnViewModel(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val appContext: Context
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

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
        try {
            val resp = api.service.config()
            if (resp.ok && resp.data != null) {
                tokenStore.wgConfig = resp.data.config
                _ui.update { it.copy(configReady = true) }
            }
        } catch (_: Exception) {
        }
    }

    fun connect() {
        val config = tokenStore.wgConfig
        if (config.isNullOrBlank()) {
            refresh()
            return
        }
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
