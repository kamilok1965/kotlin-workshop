package coroutines.workshop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

suspend fun main(): Unit = coroutineScope {
    val api = Api(url = "https://kotlin-coroutines.herokuapp.com") // remember to change `localhost` to proper url
    val token = api.request(Register("Kamil", "tak")).token

    val times = 3
    val encryptedChannel = Channel<String>(times)
    val preparedChannel = Channel<Pair<String, PasswordPrepared>>(times)
    val decodedChannel = Channel<Pair<String, PasswordDecoded>>(times)
    val decryptedChannel = Channel<Pair<String, String>>(times)

    launch {
        while (isActive) {
            val encrypted = api.request(PasswordRequest(token)).encryptedPassword
            encryptedChannel.send(encrypted)
        }
    }


    launch(Dispatchers.Default) {
        while (isActive) {
            try {
                coroutineScope {
                    repeat(times) {
                        val decrypter = Decrypter()

                        launch(Dispatchers.IO) {
                            while (isActive) {
                                val rec = encryptedChannel.receive()
                                val res = decrypter.prepare(rec)
                                if (!isActive) break
                                preparedChannel.send(Pair(rec, res))
                            }
                        }

                        launch(Dispatchers.IO) {
                            while (isActive) {
                                val rec = preparedChannel.receive()
                                val res = decrypter.decode(rec.second)
                                if (!isActive) break
                                decodedChannel.send(Pair(rec.first, res))
                            }
                        }

                        launch(Dispatchers.IO) {
                            while (isActive) {
                                val rec = decodedChannel.receive()
                                val res = decrypter.decrypt(rec.second)
                                if (!isActive) break
                                decryptedChannel.send(Pair(rec.first, res))
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
                // Empty catch, because who cares?
            }
        }
    }

    launch {
        while (isActive) {
            val rec = decryptedChannel.receive()
            api.request(Validate(token, rec.first, rec.second))
        }
    }

    Unit
}