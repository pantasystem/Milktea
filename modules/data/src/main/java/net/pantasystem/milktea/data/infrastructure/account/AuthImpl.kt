package net.pantasystem.milktea.data.infrastructure.account

import net.pantasystem.milktea.model.account.*
import javax.inject.Inject

class AuthImpl @Inject constructor(
    val accountRepository: AccountRepository,
) : Auth, AuthById, GetAccount {


    override suspend fun check(): Boolean {
        return try {
            accountRepository.getCurrentAccount().getOrThrow()
            true
        } catch (e: UnauthorizedException) {
            false
        }
    }

    override suspend fun getCurrentAccount(): Account? {
        return runCatching {
            accountRepository.getCurrentAccount().getOrThrow()
        }.getOrNull()
    }


    override suspend fun getToken(): String? {
        return getCurrentAccount()?.token
    }

    override suspend fun check(id: Long): Boolean {
        return runCatching {
            accountRepository.get(id)
        }.getOrNull() != null
    }

    override suspend fun getToken(id: Long): String {
        return accountRepository.get(id).getOrThrow().token
    }

    override suspend fun get(id: Long): Account {
        return accountRepository.get(id).getOrThrow()
    }

}