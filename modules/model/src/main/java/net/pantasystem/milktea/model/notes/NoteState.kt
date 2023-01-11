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
    sealed interface Watching {
        object None : Watching
        data class Some(val isWatching: Boolean) : Watching
    }
}