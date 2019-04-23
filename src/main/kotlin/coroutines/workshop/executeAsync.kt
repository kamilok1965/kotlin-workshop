package coroutines.workshop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call

suspend fun <T> Call<T>.notBlockingExecute(): T? =
        withContext(Dispatchers.IO) { execute().body() }