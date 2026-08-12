package fr.quickvpn.android.vpn

import android.content.Context
import android.content.Intent
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class VpnStats(
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    val lastHandshakeEpochMillis: Long = 0
)

object VpnManager {

    private val tunnel = QuickVpnTunnel("QuickVPN")
    private var backend: GoBackend? = null
    private var bound = false

    private val _isUp = MutableStateFlow(false)
    val isUp: StateFlow<Boolean> = _isUp

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _stats = MutableStateFlow(VpnStats())
    val stats: StateFlow<VpnStats> = _stats

    fun bind(context: Context) {
        if (!bound) {
            backend = GoBackend(context.applicationContext)
            bound = true
        }
    }

    fun connect(context: Context, configText: String) {
        val intent = Intent(context, QuickVpnVpnService::class.java)
            .setAction(QuickVpnVpnService.ACTION_CONNECT)
            .putExtra(QuickVpnVpnService.EXTRA_CONFIG, configText)
        context.startForegroundService(intent)
    }

    fun disconnect(context: Context) {
        val intent = Intent(context, QuickVpnVpnService::class.java)
            .setAction(QuickVpnVpnService.ACTION_DISCONNECT)
        context.startService(intent)
    }

    fun stopService(context: Context) {
        context.stopService(Intent(context, QuickVpnVpnService::class.java))
    }

    internal fun getBackend(): GoBackend? = backend

    internal fun getTunnel(): QuickVpnTunnel = tunnel

    internal fun onTunnelState(state: Tunnel.State) {
        _isUp.value = state == Tunnel.State.UP
    }

    internal fun setError(message: String?) {
        _error.value = message
    }

    internal fun updateStats(stats: VpnStats) {
        _stats.value = stats
    }

    internal fun backendState(): Tunnel.State = tunnel.state
}
