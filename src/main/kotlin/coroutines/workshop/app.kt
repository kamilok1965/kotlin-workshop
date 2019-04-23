package coroutines.workshop

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

suspend fun main() = coroutineScope {
    val api = Api(url = "http://localhost:9000") // remember to change `localhost` to proper url

    TODO()

    while(isActive) {
        TODO()
    }
}

