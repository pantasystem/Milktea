package net.pantasystem.milktea.model.confirm

import java.io.Serializable
import java.util.*

/**
 * ユーザーに確認をとるための命令を送信するためのオブジェクト
 * メッセージや確認のタイプ、識別するためのIdが入る
 * @param title 確認のタイトル、ユーザーにも表示される
 * @param message 確認のメッセージ、ユーザーにも表示される
 * @param eventType クライアントが識別するためのタイプ
 * @param confirmId クライアントが識別するためのIdコールバックとなるConfirmEventのconfirmIdと対になる
 */
data class ConfirmCommand(
    val title: String?,
    val message: String?,
    val eventType: String,
    val args: Serializable?,
    val confirmId: String = UUID.randomUUID().toString(),
    val negativeButtonText: String? = null,
    val positiveButtonText: String? = null
): Serializable