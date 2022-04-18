package net.pantasystem.milktea.model.notes.reaction

/**
 * リアクションの履歴をページネーションするためのインターフェース
 * 読み込んだデータはReactionHistoryDataSourceへ注入する
 */
interface ReactionHistoryPaginator {

    interface Factory {
        fun create(reactionHistoryRequest: ReactionHistoryRequest): ReactionHistoryPaginator
    }

    val reactionHistoryRequest: ReactionHistoryRequest

    /**
     * 次のリアクションの履歴を取得します
     */
    suspend fun next(): Boolean
}