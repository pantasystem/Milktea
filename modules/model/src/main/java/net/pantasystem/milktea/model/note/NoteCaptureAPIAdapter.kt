package net.pantasystem.milktea.model.note

import kotlinx.coroutines.flow.Flow

/**
 * Noteの更新イベントをWebSocket経由でキャプチャーして
 * その更新イベントをキャッシュに反映するための抽象
 */
interface NoteCaptureAPIAdapter {
    fun capture(id: Note.Id): Flow<NoteDataSource.Event>
}