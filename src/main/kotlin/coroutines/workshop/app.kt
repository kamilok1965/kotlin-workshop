package coroutines.workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

suspend fun main() = supervisorScope {

    val api = Api(url = "http://kotlin-coroutines.herokuapp.com") // remember to change `localhost` to proper url
    val register = Register("Kordyjan", "VirtusLab")


    val token = api.request(register).token

    val channelEncryptedPasswords = Channel<EncryptedPassword>()
    val channelDecryptedPasswords = Channel<Validate>()



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
            println("restarting")
            coroutineScope {
                (1..4).forEach { n ->
                    launch {
                        val decrypter = Decrypter()
                        while (isActive) {
                            val encryptedPassword = channelEncryptedPasswords.receive()
                            yield()
                            val prepared = decrypter.prepare(encryptedPassword.encryptedPassword)
                            yield()
                            val decoded = decrypter.decode(prepared)
                            yield()
                            val passwordDecrypted = decrypter.decrypt(decoded)
                            yield()
                            channelDecryptedPasswords.send(Validate(token, encryptedPassword.encryptedPassword, passwordDecrypted))

                        }
                    }.invokeOnCompletion { println("Worker #$n: $it") }
                }
            }
        } catch (_: Throwable) {
            println("Restart needed!")
        }
    }
}