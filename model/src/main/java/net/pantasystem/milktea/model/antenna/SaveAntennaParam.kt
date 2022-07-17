package net.pantasystem.milktea.model.antenna

import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList

/**
 * @param withReplies 返信を含むのか
 * @param name アンテナの名称
 * @param src 受信するソース "home" "all" "users" "list" "group"
 * @param userListId ユーザーのリストのユーザーのノートを取得リソースにする
 * @param userGroupId 指定したグループのユーザーのノートを取得ソースにする
 * @param keywords 受信キーワード [["key", "words"]] "key" & "words"になる[["key", "words"], ["source"]]の場合は "key" and "words" or "source"になる
 * @param excludeKeywords 除外ワード 指定方式は受信キーワードと同じ
 * @param users usernameを配列で指定する
 * @param caseSensitive
 * @param withFile ファイルが添付されたーノートのみ
 * @param notify 新しいノートを通知する
 */
data class SaveAntennaParam(
    val name: String,
    val src: AntennaSource,
    val userListId: UserList.Id?,
    val userGroupId: Group.Id?,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String>,
    val caseSensitive: Boolean = true,
    val withFile: Boolean = false,
    val withReplies: Boolean = false,
    val notify: Boolean = false,
    val hasUnreadNote: Boolean = false,
)

sealed interface AntennaSource {
    object Home : AntennaSource
    object All : AntennaSource
    object Users : AntennaSource
    object List : AntennaSource
    object Group : AntennaSource
    companion object
}

fun AntennaSource.str(): String {
    return when(this) {
        AntennaSource.All -> "all"
        AntennaSource.Group -> "group"
        AntennaSource.Home -> "home"
        AntennaSource.List -> "list"
        AntennaSource.Users -> "users"
    }
}

fun AntennaSource.Companion.values(): List<AntennaSource> {
    return listOf(
        AntennaSource.Home,
        AntennaSource.All,
        AntennaSource.Users,
        AntennaSource.List,
        AntennaSource.Group,
    )
}

fun AntennaSource.Companion.from(src: String): AntennaSource {
    return when(src) {
        "all" -> AntennaSource.All
        "group" -> AntennaSource.Group
        "home" -> AntennaSource.Home
        "list" -> AntennaSource.List
        "users" -> AntennaSource.Users
        else -> AntennaSource.All
    }
}