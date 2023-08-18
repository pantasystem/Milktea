package net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * リアクションPickerのユーザー設定
 * 原則としてインスタンスごととする
 */
@Entity(tableName = "reaction_user_setting", primaryKeys=["reaction", "instance_domain"])
data class ReactionUserSetting(
    @ColumnInfo("reaction")
    val reaction: String,

    @ColumnInfo(name = "instance_domain")
    val instanceDomain: String,

    @ColumnInfo(name = "weight")
    var weight: Int
)