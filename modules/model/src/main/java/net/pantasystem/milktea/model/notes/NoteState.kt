package net.pantasystem.milktea.model.notes

/**
 * お気に入りやスレッドのミュートの状態を表す。
 * v13でノートのウォッチ機能が削除されたため、isWatchingのフィールドも無効(None)になる。
 */
data class NoteState(
    val isFavorited: Boolean,
    val isWatching: Watching,
    val isMutedThread: Boolean?
) {

    /**
     * isWatchingの状態を3値で表現する必要が出てきたため、sealed interfaceで表現している。
     */
    sealed interface Watching {
        /**
         * APIから削除された状態で、JSONのフィールド上に存在しない。
         */
        object None : Watching

        /**
         * API上にまだ存在している状態で、JSONのフィールド上に存在する。
         * またその結果が isWatching に格納される。
         */
        data class Some(val isWatching: Boolean) : Watching
    }
}