package coroutines.workshop

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class Register(val name: String, val team: String)
data class Registered(val token: String)

data class PasswordRequest(val token: String)
data class EncryptedPassword(val encryptedPassword: String)

data class Validate(val token: String, val encryptedPassword: String, val decryptedPassword: String)

interface Api {
    @POST("/register")
    fun register(@Body request: Register): Call<Registered>

    @POST("/send-encrypted-password")
    fun requestPassword(@Body request: PasswordRequest): Call<EncryptedPassword>

    @POST("/validate")
    fun validate(@Body request: Validate): Call<Any>

    companion object {
        operator fun invoke(url: String): Api =
                Retrofit.Builder()
                        .addConverterFactory(MoshiConverterFactory.create())
                        .baseUrl(url)
                        .build()
                        .create(Api::class.java)
    }
}