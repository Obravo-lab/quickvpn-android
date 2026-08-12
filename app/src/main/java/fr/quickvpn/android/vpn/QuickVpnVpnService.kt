package fr.quickvpn.android.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.StringReader

class QuickVpnVpnService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var statsJob: Job? = null
    private var started = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        VpnManager.bind(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val configText = intent.getStringExtra(EXTRA_CONFIG)
                if (configText.isNullOrBlank()) {
                    VpnManager.setError("Configuration manquante")
                    stopSelf()
                    return START_NOT_STICKY
                }
                startAsForeground()
                scope.launch {
                    connect(configText)
                }
            }

            ACTION_DISCONNECT -> {
                scope.launch {
                    disconnect()
                    stopSelf()
                }
            }

            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private suspend fun connect(configText: String) {
        val backend = VpnManager.getBackend() ?: return
        try {
            val config = Config.parse(BufferedReader(StringReader(configText)))
            backend.setState(VpnManager.getTunnel(), Tunnel.State.UP, config)
            VpnManager.onTunnelState(VpnManager.backendState())
            VpnManager.setError(null)
            startStatsPolling(backend)
        } catch (e: Exception) {
            Log.e(TAG, "Connect failed", e)
            VpnManager.setError(e.message ?: "Échec de connexion VPN")
            VpnManager.onTunnelState(Tunnel.State.DOWN)
            stopSelf()
        }
    }

    private suspend fun disconnect() {
        val backend = VpnManager.getBackend() ?: return
        try {
            backend.setState(VpnManager.getTunnel(), Tunnel.State.DOWN, null)
        } catch (e: Exception) {
            Log.w(TAG, "Disconnect failed", e)
        }
        statsJob?.cancel()
        VpnManager.onTunnelState(Tunnel.State.DOWN)
    }

    private suspend fun startStatsPolling(backend: com.wireguard.android.backend.Backend) {
        statsJob?.cancel()
        statsJob = scope.launch {
            while (isActive) {
                try {
                    val s = backend.getStatistics(VpnManager.getTunnel())
                    VpnManager.updateStats(
                        VpnStats(
                            rxBytes = s.totalRx(),
                            txBytes = s.totalTx(),
                            lastHandshakeEpochMillis = 0
                        )
                    )
                } catch (_: Exception) {
                }
                delay(2000)
            }
        }
    }

    private fun startAsForeground() {
        started = true
        val intent = Intent(this, QuickVpnVpnService::class.java).setAction(ACTION_DISCONNECT)
        val pi = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("QuickVPN")
            .setContentText("VPN actif — Canada")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pi)
            .build()
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "QuickVPN", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onDestroy() {
        scope.launch { disconnect() }
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "QuickVpnService"
        private const val CHANNEL_ID = "quickvpn_vpn"
        private const val NOTIF_ID = 42
        const val ACTION_CONNECT = "fr.quickvpn.android.CONNECT"
        const val ACTION_DISCONNECT = "fr.quickvpn.android.DISCONNECT"
        const val ACTION_STOP = "fr.quickvpn.android.STOP"
        const val EXTRA_CONFIG = "config"
    }
}
