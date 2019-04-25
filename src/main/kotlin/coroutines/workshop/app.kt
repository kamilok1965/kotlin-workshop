package coroutines.workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import sun.plugin.dom.exception.InvalidStateException
import kotlin.coroutines.CoroutineContext

lateinit var api: Api
lateinit var token: String
val channelIn = Channel<String>(4)
var channelOut = Channel<Pair<String, String>>(Channel.UNLIMITED)

object OurContext : CoroutineScope {

    val job: Job by lazy { SupervisorJob() }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun fail() {
        coroutineContext.cancelChildren()
    }
}

@ExperimentalCoroutinesApi
suspend fun main(): Unit = coroutineScope {
    api = Api(url = "https://kotlin-coroutines.herokuapp.com/") // remembe remember to change `localhost` to proper url
    token = withContext(Dispatchers.Default) {
        api.request(Register("Johann Georg Faust", "#pato"))
    }.token

    for (a in 0..3) {
        launch {
            var encrypted = channelIn.receive()
            var prepared: PasswordPrepared? = null
            var decoded: PasswordDecoded? = null
            while (true) {
                val decrypter = Decrypter()
                OurContext.launch {
                    while (true) {
                        try {
                            if (!isActive) {
                                break
                            }
                            prepared = prepared ?: decrypter.prepare(encrypted).takeIf { isActive }
                                    ?: break
                            decoded = decoded ?: decrypter.decode(prepared!!).takeIf { isActive }
                                    ?: break
                            val decrypted = decrypter.decrypt(decoded!!).takeIf { isActive } ?: break
                            channelOut.send(Pair(encrypted, decrypted))
                            prepared = null
                            decoded = null
                            encrypted = channelIn.receive()
                        } catch (e: IllegalStateException) {
                            OurContext.fail()
                        } catch (e: java.net.UnknownHostException) {

                        }
                    }
                }.join()
            }
        }

    }
    launch {
        while (true) {
            channelIn.send(api.request(PasswordRequest(token)).encryptedPassword)
        }
    }
    launch {
        while (true) {
            try {
                channelOut.receive().let { (encrypted, decrypted) ->
                    api.request(Validate(token, encrypted, decrypted))
                }
            } catch (e: kotlinx.coroutines.channels.ClosedReceiveChannelException) {

            }
        }
    }

    Unit
}
