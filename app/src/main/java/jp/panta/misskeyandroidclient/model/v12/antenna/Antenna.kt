package jp.panta.misskeyandroidclient.model.v12.antenna

/**
 * @param i ユーザーのの認証
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
data class Antenna(
    val i: String,
    val name: String,
    val src: String,
    val userListId: String?,
    val userGroupId: String?,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String> = emptyList(),
    val caseSensitive: Boolean = true,
    val withFile: Boolean = false,
    val withReplies: Boolean = false,
    val notify: Boolean = false,
    val hasUnreadNote: Boolean = false

)