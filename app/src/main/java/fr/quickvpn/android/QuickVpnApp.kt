package fr.quickvpn.android

import android.app.Application
import fr.quickvpn.android.core.security.TokenStore

class QuickVpnApp : Application() {

    lateinit var tokenStore: TokenStore
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(this)
    }
}
