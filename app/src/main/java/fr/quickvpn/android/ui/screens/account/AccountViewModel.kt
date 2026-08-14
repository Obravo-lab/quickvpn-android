package fr.quickvpn.android.ui.screens.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import fr.quickvpn.android.core.network.ApiClient
import fr.quickvpn.android.core.network.ApiEnvelope
import fr.quickvpn.android.core.network.ChangePasswordRequest
import fr.quickvpn.android.core.network.DeleteAccountRequest
import fr.quickvpn.android.core.network.User
import fr.quickvpn.android.core.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AccountUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val passwordCurrent: String = "",
    val passwordNew: String = "",
    val passwordConfirm: String = "",
    val passwordError: String? = null,
    val passwordSuccess: Boolean = false,
    val cancelError: String? = null,
    val cancelInfo: String? = null,
    val deleting: Boolean = false,
    val deleteError: String? = null,
    val deleted: Boolean = false,
    val loggedOut: Boolean = false
)

class AccountViewModel(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val appContext: Context
) : ViewModel() {

    private val _ui = MutableStateFlow(AccountUiState())
    val ui: StateFlow<AccountUiState> = _ui

    init {
        refresh()
    }

    fun refresh() {
        _ui.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                val resp = api.service.me()
                val user = resp.data?.user
                if (resp.ok && user != null) {
                    _ui.update { it.copy(loading = false, user = user) }
                } else {
                    _ui.update { it.copy(loading = false) }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false) }
            }
        }
    }

    fun onPasswordCurrent(v: String) = _ui.update { it.copy(passwordCurrent = v) }
    fun onPasswordNew(v: String) = _ui.update { it.copy(passwordNew = v) }
    fun onPasswordConfirm(v: String) = _ui.update { it.copy(passwordConfirm = v) }

    fun changePassword() {
        val s = _ui.value
        _ui.update { it.copy(passwordError = null, passwordSuccess = false) }
        if (s.passwordNew != s.passwordConfirm) {
            _ui.update { it.copy(passwordError = "pw_mismatch") }
            return
        }
        if (s.passwordNew.length < 8) {
            _ui.update { it.copy(passwordError = "pw_short") }
            return
        }
        viewModelScope.launch {
            try {
                val resp = api.service.changePassword(
                    body = ChangePasswordRequest(s.passwordCurrent, s.passwordNew, s.passwordConfirm)
                )
                if (resp.ok) {
                    _ui.update {
                        it.copy(
                            passwordCurrent = "",
                            passwordNew = "",
                            passwordConfirm = "",
                            passwordSuccess = true
                        )
                    }
                } else {
                    _ui.update { it.copy(passwordError = resp.error?.message ?: "generic") }
                }
            } catch (e: HttpException) {
                _ui.update { it.copy(passwordError = parseError(e) ?: "generic") }
            } catch (e: Exception) {
                _ui.update { it.copy(passwordError = "network") }
            }
        }
    }

    fun cancelSubscription() {
        _ui.update { it.copy(cancelError = null, cancelInfo = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.cancelSubscription()
                if (resp.ok) {
                    val endsOn = resp.data?.endsOn
                    _ui.update {
                        it.copy(
                            cancelInfo = "cancelled",
                            user = resp.data?.user ?: it.user
                        )
                    }
                } else {
                    _ui.update { it.copy(cancelError = resp.error?.message) }
                }
            } catch (e: HttpException) {
                _ui.update { it.copy(cancelError = parseError(e) ?: "generic") }
            } catch (e: Exception) {
                _ui.update { it.copy(cancelError = "network") }
            }
        }
    }

    fun deleteAccount(password: String, confirmWord: String) {
        _ui.update { it.copy(deleting = true, deleteError = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.deleteAccount(
                    body = DeleteAccountRequest(password, confirmWord)
                )
                if (resp.ok) {
                    api.onLoggedOut()
                    _ui.update { it.copy(deleting = false, deleted = true) }
                } else {
                    _ui.update { it.copy(deleting = false, deleteError = resp.error?.message ?: "generic") }
                }
            } catch (e: HttpException) {
                _ui.update { it.copy(deleting = false, deleteError = parseError(e) ?: "generic") }
            } catch (e: Exception) {
                _ui.update { it.copy(deleting = false, deleteError = "network") }
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

    private fun parseError(e: HttpException): String? =
        runCatching {
            e.response()?.errorBody()?.string()?.let { raw ->
                Gson().fromJson(raw, ApiEnvelope::class.java)?.error?.message
            }
        }.getOrNull()
}
