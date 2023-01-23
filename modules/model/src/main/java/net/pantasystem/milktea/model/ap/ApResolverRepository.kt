package net.pantasystem.milktea.model.ap

interface ApResolverRepository {
    suspend fun resolve(accountId: Long, uri: String): Result<ApResolver>
}