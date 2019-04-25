package coroutines.workshop

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

suspend fun main(): Unit = coroutineScope {
    val api = Api(url = "https://kotlin-coroutines.herokuapp.com/")
    val registered = api.request(Register("${System.getProperty("user.name")} using Krzysztof's solution", "KKUG Team"))

    val decrypter = AtomicReference(Decrypter())
    fun <T> withDecrypter(op: (Decrypter) -> T): T = try {
        val myDecrypter = decrypter.get()
        val res = op(myDecrypter)
        if (myDecrypter === decrypter.get()) res else withDecrypter(op) // If decrypter is no longer valid, restart
    } catch (e: Exception) {
        println("Got $e")
        decrypter.set(Decrypter())
        withDecrypter(op) // Decrypter is broken, restart
    }

    val toProcess = Channel<String>(4)
    val toSend = Channel<Validate>(20) // we don't want to block here

    fun loop(op: suspend () -> Unit) = launch { while (true) op() }

    loop {
        val pass = api.request(PasswordRequest(registered.token))
        toProcess.send(pass.encryptedPassword)
    }

    loop { api.request(toSend.receive()) }

    repeat(4) {
        loop {
            val pass = toProcess.receive()
            val prepared = withDecrypter { it.prepare(pass) }
            val decoded = withDecrypter { it.decode(prepared) }
            val decrypted = withDecrypter { it.decrypt(decoded) }

            println("Decoded: $pass to $decrypted")
            toSend.send(Validate(registered.token, pass, decrypted))
        }
    }
}

