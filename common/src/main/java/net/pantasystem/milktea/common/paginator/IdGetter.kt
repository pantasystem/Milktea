package net.pantasystem.milktea.common.paginator

/**
 * ページネーションするためのIdを取得するためのインターフェース
 * IdはAPIやデータベースにアクセスするものと直接的に対応する
 */
interface IdGetter<T> {
    suspend fun getUntilId(): T?
    suspend fun getSinceId(): T?
}
