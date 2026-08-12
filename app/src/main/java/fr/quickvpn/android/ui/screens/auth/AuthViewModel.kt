package fr.quickvpn.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.quickvpn.android.core.network.ApiClient
import fr.quickvpn.android.core.network.LoginRequest
import fr.quickvpn.android.core.network.RegisterRequest
import fr.quickvpn.android.core.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val loggedIn: Boolean = false
)

class AuthViewModel(
    private val api: ApiClient,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    fun onEmail(value: String) = _ui.update { it.copy(email = value) }
    fun onPassword(value: String) = _ui.update { it.copy(password = value) }
    fun onName(value: String) = _ui.update { it.copy(name = value) }
    fun onConfirm(value: String) = _ui.update { it.copy(confirm = value) }
    fun clearInfo() = _ui.update { it.copy(info = null, error = null) }

    fun login() {
        val s = _ui.value
        if (s.loading) return
        _ui.update { it.copy(loading = true, error = null, info = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.login(body = LoginRequest(s.email.trim(), s.password))
                val data = resp.data
                if (resp.ok && data != null) {
                    tokenStore.token = data.token
                    tokenStore.userEmail = data.user.email
                    _ui.update { it.copy(loading = false, loggedIn = true) }
                } else {
                    _ui.update {
                        it.copy(loading = false, error = resp.error?.message ?: it.error)
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = "network") }
            }
        }
    }

    fun register() {
        val s = _ui.value
        if (s.loading) return
        _ui.update { it.copy(loading = true, error = null, info = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.register(
                    body = RegisterRequest(s.name.trim(), s.email.trim(), s.password, s.confirm)
                )
                if (resp.ok) {
                    _ui.update { it.copy(loading = false, info = resp.data?.message ?: "ok") }
                } else {
                    _ui.update {
                        it.copy(loading = false, error = resp.error?.message ?: it.error)
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = "network") }
            }
        }
    }
}
