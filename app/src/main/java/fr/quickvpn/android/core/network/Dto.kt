package fr.quickvpn.android.core.network

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: ApiError? = null
)

data class ApiError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
)

data class User(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("email_verified") val emailVerified: Boolean,
    @SerializedName("subscription") val subscription: String,
    @SerializedName("subscription_end") val subscriptionEnd: String?,
    @SerializedName("billing_provider") val billingProvider: String?,
    @SerializedName("has_vpn_access") val hasVpnAccess: Boolean,
    @SerializedName("has_config") val hasConfig: Boolean,
    @SerializedName("wg_ip") val wgIp: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class LoginData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: User
)

data class RegisterData(
    @SerializedName("message") val message: String,
    @SerializedName("sent") val sent: Boolean
)

data class Plan(
    @SerializedName("amount") val amount: Double,
    @SerializedName("promo_amount") val promoAmount: Double?
)

data class PlansData(
    @SerializedName("currency") val currency: String,
    @SerializedName("monthly") val monthly: Plan,
    @SerializedName("yearly") val yearly: Plan,
    @SerializedName("promo") val promo: PromoInfo?,
    @SerializedName("capacity_full") val capacityFull: Boolean
)

data class PromoInfo(
    @SerializedName("code") val code: String,
    @SerializedName("percent") val percent: Int
)

data class MeData(
    @SerializedName("user") val user: User
)

data class ConfigData(
    @SerializedName("config") val config: String,
    @SerializedName("filename") val filename: String
)

data class PlayVerifyData(
    @SerializedName("user") val user: User?,
    @SerializedName("plan") val plan: String?,
    @SerializedName("ends_on") val endsOn: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirm") val confirm: String
)

data class PlayVerifyRequest(
    @SerializedName("purchase_token") val purchaseToken: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

data class DeleteAccountRequest(
    @SerializedName("password") val password: String,
    @SerializedName("confirm") val confirm: String
)

data class CancelData(
    @SerializedName("user") val user: User?,
    @SerializedName("ends_on") val endsOn: String?
)

data class ApiMessage(
    @SerializedName("message") val message: String?
)
