package coroutines.workshop.internal

import coroutines.workshop.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface InternalApi {
    @POST("/register")
    fun register(@Body request: Register): Call<Registered>

    @POST("/send-encrypted-password")
    fun requestPassword(@Body request: PasswordRequest): Call<EncryptedPassword>

    @POST("/validate")
    fun validate(@Body request: Validate): Call<Any>

    companion object {
        operator fun invoke(url: String): InternalApi =
                Retrofit.Builder()
                        .addConverterFactory(MoshiConverterFactory.create())
                        .baseUrl(url)
                        .build()
                        .create(InternalApi::class.java)
    }
}