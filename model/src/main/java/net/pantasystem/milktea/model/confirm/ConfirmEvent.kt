package net.pantasystem.milktea.model.confirm

import java.io.Serializable

/**
 * @param resultType ユーザーが選択した結果
 * @param eventType クライアントが識別する
 * @param confirmId クライアントが識別するためのId ConfirmCommandで送信したIdがここに入る
 */
data class ConfirmEvent(
    val resultType: ResultType,
    val eventType: String,
    val confirmId: String,
    val args: Serializable?
): Serializable