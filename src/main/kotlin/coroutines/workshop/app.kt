package coroutines.workshop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

suspend fun main(): Unit = coroutineScope {
    val api = Api(url = "https://kotlin-coroutines.herokuapp.com/")
    val token = api.request(Register("Krystian", "KKUG Team")).token
    val channelEncryptedPasswords = Channel<EncryptedPassword>(4)
    val channelDecryptedPasswords = Channel<Validate>(20)

    launch(Dispatchers.IO) {
        while (isActive) {
            val encryptedPassword = api.request(PasswordRequest(token))
            channelEncryptedPasswords.send(encryptedPassword)
        }
    }

    launch(Dispatchers.IO) {
        while (isActive) {
            val validate = channelDecryptedPasswords.receive()
            api.request(validate)
        }
    }

    while (true) {
        try {
            coroutineScope {
                (1..4).forEach {
                    launch {
                        val decrypter = Decrypter()
                        while (isActive) {
                            val encrypted = channelEncryptedPasswords.receive()
                            val prepared = decrypter.prepare(encrypted.encryptedPassword)
                            val decoded = decrypter.decode(prepared)
                            val decrypted = decrypter.decrypt(decoded)
                            channelDecryptedPasswords.send(Validate(token, encrypted.encryptedPassword, decrypted))
                        }
                    }.invokeOnCompletion { println(it) }
                }
            }
        } catch (_: Throwable) {
            println("catched!")
        }
    }
}

