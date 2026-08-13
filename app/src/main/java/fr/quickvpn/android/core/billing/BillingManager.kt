package fr.quickvpn.android.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BillingState(
    val ready: Boolean = false,
    val products: List<ProductDetails> = emptyList(),
    val error: String? = null,
    val purchased: Purchase? = null
)

object BillingManager : PurchasesUpdatedListener {

    private const val PRODUCT_MONTHLY = "monthly"
    private const val PRODUCT_YEARLY = "yearly"

    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state

    private var client: BillingClient? = null
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        if (client != null) return
        client = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        client!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(ready = true, error = null)
                    queryProducts()
                    checkPendingPurchases()
                } else {
                    _state.value = _state.value.copy(ready = false, error = "billing_unavailable")
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.value = _state.value.copy(ready = false)
            }
        })
    }

    private fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        client?.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _state.value = _state.value.copy(products = details.orEmpty())
            }
        }
    }

    private fun checkPendingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        client?.queryPurchasesAsync(params) { _, purchases ->
            purchases.lastOrNull()?.let { handlePurchase(it) }
        }
    }

    fun purchase(activity: Activity, plan: String) {
        val product = _state.value.products.firstOrNull {
            it.productId == if (plan == "yearly") PRODUCT_YEARLY else PRODUCT_MONTHLY
        } ?: return
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
        val paramsBuilder = BillingFlowParams.newBuilder()
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
        if (offerToken != null) {
            productParams.setOfferToken(offerToken)
        }
        paramsBuilder.setProductDetailsParamsList(listOf(productParams.build()))
        client?.launchBillingFlow(activity, paramsBuilder.build())
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.lastOrNull()?.let { handlePurchase(it) }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _state.value = _state.value.copy(error = "billing_cancelled")
            }

            else -> {
                _state.value = _state.value.copy(error = "billing_error")
            }
        }
    }

    fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _state.value = _state.value.copy(purchased = purchase)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _state.value = _state.value.copy(error = "billing_pending")
        }
    }

    fun acknowledge(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        client?.acknowledgePurchase(params) {}
    }

    fun clearPurchased() {
        _state.value = _state.value.copy(purchased = null, error = null)
    }
}
