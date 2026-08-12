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

    object EmptyBody
}
