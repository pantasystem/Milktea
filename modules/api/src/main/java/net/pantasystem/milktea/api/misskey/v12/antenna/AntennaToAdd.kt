package net.pantasystem.milktea.api.misskey.v12.antenna

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.antenna.SaveAntennaParam
import net.pantasystem.milktea.model.antenna.str

/**
 * @param i ユーザーのの認証
 * @param antennaId updateのときはUpdate対象のアンテナのIdを指定します
 * @param withReplies 返信を含むのか
 * @param name アンテナの名称
 * @param src 受信するソース "home" "all" "users" "list" "group"
 * @param userListId ユーザーのリストのユーザーのノートを取得リソースにする
 * @param userGroupId 指定したグループのユーザーのノートを取得ソースにする
 * @param keywords 受信キーワード [["key", "words"]] "key" & "words"になる[["key", "words"], ["source"]]の場合は "key" and "words" or "source"になる
 * @param excludeKeywords 除外ワード 指定方式は受信キーワードと同じ
 * @param users ユーザーを指定する
 * @param caseSensitive
 * @param withFile ファイルが添付されたーノートのみ
 * @param notify 新しいノートを通知する
 */
@Serializable
data class AntennaToAdd(
    val i: String,
    val antennaId: String? = null,
    val name: String,
    val src: String,
    val userListId: String? = null,
    val userGroupId: String? = null,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String>,
    val caseSensitive: Boolean,
    val withFile: Boolean,
    val withReplies: Boolean,
    val notify: Boolean,
    val hasUnreadNote: Boolean = false

) {
    companion object
}


fun AntennaToAdd.Companion.from(
    i: String,
    params: SaveAntennaParam,
    antennaId: String? = null
): AntennaToAdd {
    return AntennaToAdd(
        i,
        antennaId,
        params.name,
        params.src.str(),
        params.userListId?.userListId,
        params.userGroupId?.groupId,
        params.keywords,
        params.excludeKeywords,
        params.users,
        params.caseSensitive,
        params.withFile,
        params.withReplies,
        params.notify,
        params.hasUnreadNote,
    )
}