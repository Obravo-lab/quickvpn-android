package fr.quickvpn.android.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.quickvpn.android.core.network.ApiClient
import fr.quickvpn.android.core.network.User
import fr.quickvpn.android.core.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val loggedOut: Boolean = false
)

class DashboardViewModel(
    private val api: ApiClient,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _ui = MutableStateFlow(DashboardUiState())
    val ui: StateFlow<DashboardUiState> = _ui

    init {
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
                } else {
                    _ui.update { it.copy(loading = false, error = resp.error?.message) }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = "network") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                api.service.logout()
            } catch (_: Exception) {
            }
            api.onLoggedOut()
            _ui.update { it.copy(loggedOut = true) }
        }
    }
}
