package fr.quickvpn.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import fr.quickvpn.android.core.network.ApiClient
import fr.quickvpn.android.ui.navigation.AppNavHost
import fr.quickvpn.android.ui.navigation.LocalApplication
import fr.quickvpn.android.ui.theme.QuickVpnTheme

object AppGraph {
    val ApiClient = staticCompositionLocalOf<fr.quickvpn.android.core.network.ApiClient> {
        error("ApiClient non fourni")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickVpnTheme {
                val app = application as fr.quickvpn.android.QuickVpnApp
                val apiClient = remember { ApiClient(app.tokenStore) }
                CompositionLocalProvider(
                    AppGraph.ApiClient provides apiClient,
                    LocalApplication provides app
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
