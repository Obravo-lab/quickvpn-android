package fr.quickvpn.android.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/v1/index.php")
    suspend fun login(
        @Query("action") action: String = "auth/login",
        @Body body: LoginRequest
    ): ApiEnvelope<LoginData>

    @POST("api/v1/index.php")
    suspend fun register(
        @Query("action") action: String = "auth/register",
        @Body body: RegisterRequest
    ): ApiEnvelope<RegisterData>

    @GET("api/v1/index.php")
    suspend fun plans(
        @Query("action") action: String = "plans"
    ): ApiEnvelope<PlansData>

    @GET("api/v1/index.php")
    suspend fun me(
        @Query("action") action: String = "me"
    ): ApiEnvelope<MeData>

    @GET("api/v1/index.php")
    suspend fun config(
        @Query("action") action: String = "config"
    ): ApiEnvelope<ConfigData>

    @POST("api/v1/index.php")
    suspend fun configGenerate(
        @Query("action") action: String = "config/generate",
        @Body body: Any = EmptyBody
    ): ApiEnvelope<ConfigData>

    @POST("api/v1/index.php")
    suspend fun logout(
        @Query("action") action: String = "auth/logout",
        @Body body: Any = EmptyBody
    ): ApiEnvelope<Any>

    @POST("api/v1/index.php")
    suspend fun playVerify(
        @Query("action") action: String = "play/verify",
        @Body body: PlayVerifyRequest
    ): ApiEnvelope<PlayVerifyData>

    @POST("api/v1/index.php")
    suspend fun changePassword(
        @Query("action") action: String = "account/password",
        @Body body: ChangePasswordRequest
    ): ApiEnvelope<ApiMessage>

    @POST("api/v1/index.php")
    suspend fun cancelSubscription(
        @Query("action") action: String = "subscription/cancel",
        @Body body: Any = EmptyBody
    ): ApiEnvelope<CancelData>

    @POST("api/v1/index.php")
    suspend fun deleteAccount(
        @Query("action") action: String = "account/delete",
        @Body body: DeleteAccountRequest
    ): ApiEnvelope<Any>

    object EmptyBody
}
