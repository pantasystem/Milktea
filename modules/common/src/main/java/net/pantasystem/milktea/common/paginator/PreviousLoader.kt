package net.pantasystem.milktea.common.paginator

/**
 * 過去のページを取得するためのAPIのアダプター
 */
interface PreviousLoader<DTO> {
    suspend fun loadPrevious(): Result<List<DTO>>
}
