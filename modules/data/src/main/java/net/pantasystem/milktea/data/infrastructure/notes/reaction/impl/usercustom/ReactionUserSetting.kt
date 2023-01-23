package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * リアクションPickerのユーザー設定
 * 原則としてインスタンスごととする
 */
@Entity(tableName = "reaction_user_setting", primaryKeys=["reaction", "instance_domain"])
data class ReactionUserSetting(
    val reaction: String,

    @ColumnInfo(name = "instance_domain")
    val instanceDomain: String,
    var weight: Int
)