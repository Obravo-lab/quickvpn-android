package fr.quickvpn.android.ui.screens.plans

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.quickvpn.android.R
import fr.quickvpn.android.core.network.Plan
import fr.quickvpn.android.ui.navigation.viewModelFactory
import fr.quickvpn.android.ui.theme.CtaButton
import fr.quickvpn.android.ui.theme.CtaOutlinedButton
import fr.quickvpn.android.ui.theme.SurfaceFeatured
import java.util.Locale

@Composable
fun PlansScreen(
    onBack: () -> Unit,
    vm: PlansViewModel = viewModel<PlansViewModel>(factory = viewModelFactory<PlansViewModel>())
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.plans_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.plans_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        if (state.verifying) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.plans_verify),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.subscribed) {
            Text(
                text = stringResource(R.string.plans_subscribed),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            CtaButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.plans_back))
            }
            return@Column
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = billingErrorText(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val monthly = state.monthly
        val yearly = state.yearly
        val promo = state.promo

        if (monthly != null) {
            PlanCard(
                title = stringResource(R.string.plans_monthly),
                price = formatPrice(monthly),
                promoText = promo?.let {
                    stringResource(R.string.plans_promo, it.percent)
                },
                featured = false,
                onSubscribe = { if (activity != null) vm.buy("monthly", activity) },
                enabled = state.availablePlans.contains("monthly") && !state.loading
            )
            Spacer(Modifier.height(16.dp))
        }

        if (yearly != null) {
            PlanCard(
                title = stringResource(R.string.plans_yearly),
                price = formatPrice(yearly),
                promoText = promo?.let {
                    stringResource(R.string.plans_promo, it.percent)
                },
                featured = true,
                onSubscribe = { if (activity != null) vm.buy("yearly", activity) },
                enabled = state.availablePlans.contains("yearly") && !state.loading
            )
        }

        Spacer(Modifier.height(24.dp))
        CtaOutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.plans_back))
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    promoText: String?,
    featured: Boolean,
    enabled: Boolean,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (featured) {
                SurfaceFeatured
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (featured) {
                    Text(
                        text = stringResource(R.string.plans_best_value),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = price,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            promoText?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(12.dp))
            CtaButton(
                onClick = onSubscribe,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(stringResource(R.string.plans_subscribe))
            }
        }
    }
}

@Composable
private fun billingErrorText(key: String): String = when (key) {
    "network" -> stringResource(R.string.error_network)
    "billing_unavailable" -> stringResource(R.string.billing_unavailable)
    "billing_error" -> stringResource(R.string.billing_error)
    "billing_cancelled" -> stringResource(R.string.billing_cancelled)
    "billing_pending" -> stringResource(R.string.billing_pending)
    else -> key
}

private fun formatPrice(plan: Plan): String {
    val amount = plan.promoAmount ?: plan.amount
    return String.format(Locale.getDefault(), "%.2f €", amount)
}
