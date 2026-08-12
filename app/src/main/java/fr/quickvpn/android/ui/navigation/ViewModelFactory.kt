package fr.quickvpn.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.quickvpn.android.AppGraph
import fr.quickvpn.android.ui.screens.auth.AuthViewModel
import fr.quickvpn.android.ui.screens.dashboard.DashboardViewModel

@Composable
inline fun <reified VM : ViewModel> viewModelFactory(): ViewModelProvider.Factory {
    val api = AppGraph.ApiClient.current
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                VM::class.java.isAssignableFrom(modelClass) && VM::class == AuthViewModel::class ->
                    AuthViewModel(api, api.tokenStore) as T
                VM::class.java.isAssignableFrom(modelClass) && VM::class == DashboardViewModel::class ->
                    DashboardViewModel(api, api.tokenStore) as T
                else -> error("ViewModel inconnu: ${modelClass.name}")
            }
        }
    }
}
