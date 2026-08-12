package fr.quickvpn.android.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.quickvpn.android.AppGraph
import fr.quickvpn.android.ui.screens.auth.AuthViewModel
import fr.quickvpn.android.ui.screens.home.HomeVpnViewModel

val LocalApplication = staticCompositionLocalOf<Application> {
    error("Application non fournie")
}

@Composable
inline fun <reified VM : ViewModel> viewModelFactory(): ViewModelProvider.Factory {
    val api = AppGraph.ApiClient.current
    val app = LocalApplication.current
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                VM::class == AuthViewModel::class ->
                    AuthViewModel(api, api.tokenStore) as T
                VM::class == HomeVpnViewModel::class ->
                    HomeVpnViewModel(api, api.tokenStore, app) as T
                else -> error("ViewModel inconnu: ${modelClass.name}")
            }
        }
    }
}
