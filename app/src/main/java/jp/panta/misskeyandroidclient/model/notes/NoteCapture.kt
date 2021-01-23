package jp.panta.misskeyandroidclient.model.notes

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

/**
 * ノートキャプチャーのインターフェース
 * APIサーバーへのcapture/unCaptureをする
 * また多重capture防止などの責任を持つ
 */
interface NoteCapture {

    fun observer(): Flow<NoteCaptureEvent>

    fun capture(noteId: String): Boolean

    fun unCapture(noteId: String): Boolean
}