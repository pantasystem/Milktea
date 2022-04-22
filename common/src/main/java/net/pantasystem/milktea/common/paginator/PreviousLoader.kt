package net.pantasystem.milktea.common.paginator

import retrofit2.Response

/**
 * 過去のページを取得するためのAPIのアダプター
 */
interface PreviousLoader<DTO> {
    suspend fun loadPrevious(): Response<List<DTO>>
}
