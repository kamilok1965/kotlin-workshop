package coroutines.workshop

import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    val api = Api(url = "http://localhost:9000") // remember to change `localhost` to proper url

    TODO()

    Unit
}

