package net.pantasystem.milktea.api.misskey.v12.antenna

import kotlinx.serialization.SerialName
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
    @SerialName("i")
    val i: String,

    @SerialName("antennaId")
    val antennaId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("src")
    val src: String,

    @SerialName("userListId")
    val userListId: String? = null,

    @SerialName("userGroupId")
    val userGroupId: String? = null,

    @SerialName("keywords")
    val keywords: List<List<String>>,

    @SerialName("excludeKeywords")
    val excludeKeywords: List<List<String>>,

    @SerialName("users")
    val users: List<String>,

    @SerialName("caseSensitive")
    val caseSensitive: Boolean,

    @SerialName("withFile")
    val withFile: Boolean,

    @SerialName("withReplies")
    val withReplies: Boolean,

    @SerialName("notify")
    val notify: Boolean,

    @SerialName("hasUnreadNote")
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