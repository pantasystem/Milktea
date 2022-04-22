package net.pantasystem.milktea.common.paginator

/**
 * 新たなページを取得するためのAPIのアダプター
 */
interface FutureLoader<DTO> {
    suspend fun loadFuture(): Result<List<DTO>>
}
