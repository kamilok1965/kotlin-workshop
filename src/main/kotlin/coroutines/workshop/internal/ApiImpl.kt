package coroutines.workshop.internal

import coroutines.workshop.Api
import coroutines.workshop.PasswordRequest
import coroutines.workshop.Register
import coroutines.workshop.Validate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call

class ApiImpl(url: String) : Api {
    private val internalApi = InternalApi(url)

    override suspend fun request(register: Register) =
            internalApi.register(register).notBlockingExecute()!!

    override suspend fun request(passwordRequest: PasswordRequest) =
            internalApi.requestPassword(passwordRequest).notBlockingExecute()!!

    override suspend fun request(validate: Validate) =
            internalApi.validate(validate).notBlockingExecute().let { Unit }
}

private suspend fun <T> Call<T>.notBlockingExecute(): T? =
        withContext(Dispatchers.IO) { execute().body() }