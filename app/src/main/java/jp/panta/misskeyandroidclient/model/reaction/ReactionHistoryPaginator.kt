package jp.panta.misskeyandroidclient.model.reaction

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
     * 未来のリアクション履歴を取得します。
     */
    suspend fun loadFuture()

    /**
     * 過去のリアクション履歴を取得します。
     */
    suspend fun loadPast()
}