package fr.quickvpn.android.ui.screens.plans

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.google.gson.Gson
import fr.quickvpn.android.core.billing.BillingManager
import fr.quickvpn.android.core.network.ApiClient
import fr.quickvpn.android.core.network.ApiEnvelope
import fr.quickvpn.android.core.network.Plan
import fr.quickvpn.android.core.network.PlayVerifyRequest
import fr.quickvpn.android.core.network.PromoInfo
import fr.quickvpn.android.core.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class PlansUiState(
    val loading: Boolean = true,
    val monthly: Plan? = null,
    val yearly: Plan? = null,
    val promo: PromoInfo? = null,
    val productsReady: Boolean = false,
    val availablePlans: Set<String> = emptySet(),
    val verifying: Boolean = false,
    val subscribed: Boolean = false,
    val error: String? = null
)

class PlansViewModel(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val appContext: Context
) : ViewModel() {

    private val _ui = MutableStateFlow(PlansUiState())
    val ui: StateFlow<PlansUiState> = _ui

    private val handledTokens = mutableSetOf<String>()

    init {
        BillingManager.init(appContext)
        fetchPlans()
        viewModelScope.launch {
            BillingManager.state.collect { bs ->
                _ui.update {
                    it.copy(
                        productsReady = bs.ready,
                        availablePlans = bs.products.map { p -> p.productId }.toSet(),
                        error = bs.error ?: it.error
                    )
                }
                bs.purchased?.let { purchase ->
                    if (handledTokens.add(purchase.purchaseToken)) {
                        verifyPurchase(purchase)
                    }
                }
            }
        }
    }

    fun fetchPlans() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.plans()
                if (resp.ok && resp.data != null) {
                    _ui.update {
                        it.copy(
                            loading = false,
                            monthly = resp.data.monthly,
                            yearly = resp.data.yearly,
                            promo = resp.data.promo
                        )
                    }
                } else {
                    _ui.update { it.copy(loading = false, error = resp.error?.message) }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = "network") }
            }
        }
    }

    fun buy(plan: String, activity: Activity) {
        BillingManager.purchase(activity, plan)
    }

    private fun verifyPurchase(purchase: Purchase) {
        _ui.update { it.copy(verifying = true, error = null) }
        viewModelScope.launch {
            try {
                val resp = api.service.playVerify(body = PlayVerifyRequest(purchase.purchaseToken))
                if (resp.ok) {
                    BillingManager.acknowledge(purchase.purchaseToken)
                    _ui.update { it.copy(verifying = false, subscribed = true) }
                } else {
                    _ui.update { it.copy(verifying = false, error = resp.error?.message) }
                }
            } catch (e: HttpException) {
                val message = runCatching {
                    e.response()?.errorBody()?.string()?.let { raw ->
                        Gson().fromJson(raw, ApiEnvelope::class.java)?.error?.message
                    }
                }.getOrNull()
                _ui.update { it.copy(verifying = false, error = message ?: "network") }
            } catch (e: Exception) {
                _ui.update { it.copy(verifying = false, error = "network") }
            }
        }
    }
}
