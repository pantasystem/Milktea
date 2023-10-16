@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.pantasystem.milktea.data.infrastructure.account.db.AccountDAO
import net.pantasystem.milktea.data.infrastructure.account.db.AccountInstanceTypeConverter
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.data.infrastructure.account.page.db.PageDAO
import net.pantasystem.milktea.data.infrastructure.account.page.db.PageRecord
import net.pantasystem.milktea.data.infrastructure.account.page.db.TimelinePageTypeConverter
import net.pantasystem.milktea.data.infrastructure.core.*
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecordDao
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiAliasRecord
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiRecord
import net.pantasystem.milktea.data.infrastructure.filter.db.MastodonFilterDao
import net.pantasystem.milktea.data.infrastructure.filter.db.MastodonWordFilterRecord
import net.pantasystem.milktea.data.infrastructure.group.GroupDao
import net.pantasystem.milktea.data.infrastructure.group.GroupMemberIdRecord
import net.pantasystem.milktea.data.infrastructure.group.GroupMemberView
import net.pantasystem.milktea.data.infrastructure.group.GroupRecord
import net.pantasystem.milktea.data.infrastructure.instance.db.*
import net.pantasystem.milktea.data.infrastructure.list.UserListDao
import net.pantasystem.milktea.data.infrastructure.list.UserListMemberIdRecord
import net.pantasystem.milktea.data.infrastructure.list.UserListMemberView
import net.pantasystem.milktea.data.infrastructure.list.UserListRecord
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoDao
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoRecord
import net.pantasystem.milktea.data.infrastructure.note.draft.db.*
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.history.ReactionHistoryDao
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.history.ReactionHistoryRecord
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.data.infrastructure.note.wordmute.WordFilterConditionRecord
import net.pantasystem.milktea.data.infrastructure.note.wordmute.WordFilterConditionRegexRecord
import net.pantasystem.milktea.data.infrastructure.note.wordmute.WordFilterConditionWordRecord
import net.pantasystem.milktea.data.infrastructure.note.wordmute.WordFilterConfigDao
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationJsonCacheRecord
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationJsonCacheRecordDAO
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotification
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.search.SearchHistoryDao
import net.pantasystem.milktea.data.infrastructure.search.SearchHistoryRecord
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewRecord
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameDAO
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameDTO
import net.pantasystem.milktea.data.infrastructure.user.db.*
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord

@Database(
    entities = [
        EncryptedConnectionInformation::class,
        ReactionHistoryRecord::class,
        Account::class,
        ReactionUserSetting::class,
        Page::class,
        PollChoiceDTO::class,
        UserIdDTO::class,
        DraftNoteDTO::class,

        UrlPreviewRecord::class,
        AccountRecord::class,
        PageRecord::class,
        MetaDTO::class,
//        EmojiDTO::class,
//        EmojiAlias::class,
        UnreadNotification::class,
        UserNicknameDTO::class,
        DriveFileRecord::class,
        DraftFileJunctionRef::class,
        DraftLocalFile::class,
        GroupRecord::class,
        GroupMemberIdRecord::class,
        UserRecord::class,
        UserDetailedStateRecord::class,
        UserEmojiRecord::class,
        PinnedNoteIdRecord::class,
        UserInstanceInfoRecord::class,
        UserProfileFieldRecord::class,

        WordFilterConditionRecord::class,
        WordFilterConditionRegexRecord::class,
        WordFilterConditionWordRecord::class,

        UserListRecord::class,
        UserListMemberIdRecord::class,
        InstanceInfoRecord::class,

        SearchHistoryRecord::class,
        UserInfoStateRecord::class,
        UserRelatedStateRecord::class,
        NodeInfoRecord::class,

        MastodonInstanceInfoRecord::class,

//        CustomEmojiRecord::class,
//        CustomEmojiAliasRecord::class,

        NotificationJsonCacheRecord::class,

        MastodonWordFilterRecord::class,

        RenoteMuteRecord::class,

        FedibirdCapabilitiesRecord::class,

        PleromaMetadataFeatures::class,

        CustomEmojiRecord::class,
        CustomEmojiAliasRecord::class,
    ],
    version = 56,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
        AutoMigration(from = 24, to = 25),
        AutoMigration(from = 25, to = 26),
        AutoMigration(from = 26, to = 27),
        AutoMigration(from = 27, to = 28),
        AutoMigration(from = 28, to = 29),
        AutoMigration(from = 29, to = 30),
        AutoMigration(from = 30, to = 31),
        AutoMigration(from = 31, to = 32),
        AutoMigration(from = 32, to = 33),
        AutoMigration(from = 33, to = 34),
        AutoMigration(from = 34, to = 35),
        AutoMigration(from = 35, to = 36),
        AutoMigration(from = 36, to = 37),
        AutoMigration(from = 37, to = 38),
        AutoMigration(from = 38, to = 39),
        AutoMigration(from = 39, to = 40),
        AutoMigration(from = 40, to = 41),
        AutoMigration(from = 41, to = 42),
        AutoMigration(from = 42, to = 43),
        AutoMigration(from = 43, to = 44),
        AutoMigration(from = 44, to = 45),
        AutoMigration(from = 45, to = 46),
        AutoMigration(from = 46, to = 47),
        AutoMigration(from = 47, to = 48),
        AutoMigration(from = 48, to = 49),
        AutoMigration(from = 49, to = 50),
        AutoMigration(from = 50, to = 51),
        AutoMigration(from = 52, to = 53),
        AutoMigration(from = 53, to = 54),
        AutoMigration(from = 54, to = 55),
        AutoMigration(from = 55, to = 56),
    ],
    views = [UserView::class, GroupMemberView::class, UserListMemberView::class]
)
@TypeConverters(
    PageTypeConverter::class,
    DateConverter::class,
    TimelinePageTypeConverter::class,
    AccountInstanceTypeConverter::class,
    InstantConverter::class,
    LocalDateConverter::class,
)
abstract class DataBase : RoomDatabase() {
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
    @Deprecated("pageDaoへ移行")
    abstract fun connectionInformationDao(): ConnectionInformationDao

    @Deprecated("accountDAOへ移行")
    abstract fun accountDao(): AccountDao
    abstract fun reactionHistoryDao(): ReactionHistoryDao
    abstract fun reactionUserSettingDao(): ReactionUserSettingDao

    @Deprecated("pageDaoへ移行")
    abstract fun pageDao(): PageDao
    abstract fun draftNoteDao(): DraftNoteDao

    abstract fun urlPreviewDAO(): UrlPreviewDAO

    abstract fun accountDAO(): AccountDAO
    abstract fun pageDAO(): PageDAO

    abstract fun metaDAO(): MetaDAO
//    abstract fun emojiAliasDAO(): EmojiAliasDAO

    abstract fun unreadNotificationDAO(): UnreadNotificationDAO

    abstract fun userNicknameDAO(): UserNicknameDAO


    abstract fun driveFileRecordDAO(): DriveFileRecordDao

    abstract fun groupDao(): GroupDao

    abstract fun userDao(): UserDao

    abstract fun wordFilterConfigDao(): WordFilterConfigDao

    abstract fun userListDao(): UserListDao

    abstract fun instanceInfoDao(): InstanceInfoDao

    abstract fun searchHistoryDao(): SearchHistoryDao

    abstract fun nodeInfoDao(): NodeInfoDao

    abstract fun mastodonInstanceInfoDao(): MastodonInstanceInfoDAO

//    abstract fun customEmojiDao(): CustomEmojiDAO

    abstract fun notificationJsonCacheRecordDAO(): NotificationJsonCacheRecordDAO

    abstract fun mastodonFilterDao(): MastodonFilterDao

    abstract fun renoteMuteDao(): RenoteMuteDao

    abstract fun customEmojiDao(): CustomEmojiDAO
}