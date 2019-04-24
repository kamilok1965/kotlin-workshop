package coroutines.workshop

import coroutines.workshop.internal.ApiImpl

data class Register(val name: String, val team: String)
data class Registered(val token: String)

data class PasswordRequest(val token: String)
data class EncryptedPassword(val encryptedPassword: String)

data class Validate(val token: String, val encryptedPassword: String, val decryptedPassword: String)

interface Api {
    suspend fun request(register: Register): Registered

    suspend fun request(passwordRequest: PasswordRequest): EncryptedPassword

    suspend fun request(validate: Validate)

    companion object {
        operator fun invoke(url: String): Api = ApiImpl(url)
    }
}