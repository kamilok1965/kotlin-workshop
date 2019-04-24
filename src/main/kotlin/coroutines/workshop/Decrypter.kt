package coroutines.workshop

import coroutines.workshop.internal.DecrypterImpl

sealed class DecryptionState

data class PasswordPrepared(val password: String) : DecryptionState()

data class PasswordDecoded(val password: String) : DecryptionState()

interface Decrypter {
    fun prepare(password: String): PasswordPrepared
    fun decode(state: PasswordPrepared): PasswordDecoded
    fun decrypt(state: PasswordDecoded): String

    companion object {
        operator fun invoke(): Decrypter = DecrypterImpl()
    }
}

