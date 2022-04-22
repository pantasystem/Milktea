package net.pantasystem.milktea.common.paginator

import retrofit2.Response

/**
 * 新たなページを取得するためのAPIのアダプター
 */
interface FutureLoader<DTO> {
    suspend fun loadFuture(): Response<List<DTO>>
}
