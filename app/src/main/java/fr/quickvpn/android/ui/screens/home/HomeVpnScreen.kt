package fr.quickvpn.android.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.quickvpn.android.R
import fr.quickvpn.android.ui.navigation.viewModelFactory
import fr.quickvpn.android.ui.theme.Background
import fr.quickvpn.android.ui.theme.CtaButton
import fr.quickvpn.android.ui.theme.CtaOutlinedButton
import fr.quickvpn.android.ui.theme.CtaWhite
import fr.quickvpn.android.ui.theme.Danger
import fr.quickvpn.android.ui.theme.TextLight
import java.util.Locale

@Composable
fun HomeVpnScreen(
    onLoggedOut: () -> Unit,
    onGoPlans: () -> Unit,
    onGoAccount: () -> Unit,
    vm: HomeVpnViewModel = viewModel<HomeVpnViewModel>(factory = viewModelFactory<HomeVpnViewModel>())
) {
    val state by vm.ui.collectAsState()

    if (state.loggedOut) {
        onLoggedOut()
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        val user = state.user
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (user != null) {
            Text(
                text = stringResource(R.string.home_greeting, user.name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(40.dp))

        val context = LocalContext.current
        val notifPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}
        val vpnConsentLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                vm.connect()
            }
        }
        val pendingConsent by vm.pendingConsent.collectAsState()
        LaunchedEffect(pendingConsent) {
            pendingConsent?.let {
                vpnConsentLauncher.launch(it)
                vm.clearPendingConsent()
            }
        }

        PowerButton(
            isUp = state.vpnUp,
            loading = state.loading,
            onClick = {
                if (state.vpnUp) {
                    vm.disconnect()
                } else {
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    vm.prepareConnect()
                }
            }
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(
                if (state.vpnUp) R.string.home_connected else R.string.home_disconnected
            ),
            style = MaterialTheme.typography.titleMedium,
            color = TextLight
        )
        Text(
            text = stringResource(R.string.home_server),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        state.vpnError?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        state.info?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (it == "vpn_authorize") {
                    stringResource(R.string.home_vpn_authorize)
                } else {
                    it
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(32.dp))

        if (state.vpnUp) {
            StatsRow(
                rx = state.stats.rxBytes,
                tx = state.stats.txBytes
            )
            Spacer(Modifier.height(16.dp))
        }

        user?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.dashboard_subscription),
                    value = if (it.hasVpnAccess) {
                        stringResource(R.string.dashboard_sub_active)
                    } else {
                        stringResource(R.string.dashboard_sub_inactive)
                    },
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.dashboard_vpn_ip),
                    value = it.wgIp ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (user != null && !user.hasVpnAccess) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.home_no_config),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    CtaButton(
                        onClick = onGoPlans,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(stringResource(R.string.home_subscribe))
                    }
                }
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = when (it) {
                    "network" -> stringResource(R.string.error_network)
                    "vpn_no_config" -> stringResource(R.string.home_error_no_config)
                    else -> it
                },
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            CtaOutlinedButton(onClick = vm::refresh) {
                Text(stringResource(R.string.dashboard_retry))
            }
        }

        Spacer(Modifier.height(32.dp))
        CtaOutlinedButton(onClick = onGoAccount, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.home_account))
        }
        Spacer(Modifier.height(8.dp))
        CtaOutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.dashboard_logout))
        }
    }
}

@Composable
private fun PowerButton(isUp: Boolean, loading: Boolean, onClick: () -> Unit) {
    val bg = if (isUp) Danger else CtaWhite
    val fg = if (isUp) Color.White else Background
    val borderColor = if (isUp) {
        Color(0x40DC3545)
    } else {
        Color(0x407DCEC2)
    }
    Box(
        modifier = Modifier
            .size(150.dp)
            .border(6.dp, borderColor, CircleShape)
            .background(bg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(color = fg)
        } else {
            Text(
                text = stringResource(if (isUp) R.string.home_disconnect else R.string.home_connect),
                color = fg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatsRow(rx: Long, tx: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = stringResource(R.string.home_rx),
            value = formatBytes(rx),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.home_tx),
            value = formatBytes(tx),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.1f Ko", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.getDefault(), "%.1f Mo", mb)
    val gb = mb / 1024.0
    return String.format(Locale.getDefault(), "%.2f Go", gb)
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
