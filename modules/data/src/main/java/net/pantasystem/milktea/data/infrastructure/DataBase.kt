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
import net.pantasystem.milktea.data.infrastructure.instance.db.FedibirdCapabilitiesRecord
import net.pantasystem.milktea.data.infrastructure.instance.db.InstanceInfoDao
import net.pantasystem.milktea.data.infrastructure.instance.db.InstanceInfoRecord
import net.pantasystem.milktea.data.infrastructure.instance.db.MastodonInstanceInfoDAO
import net.pantasystem.milktea.data.infrastructure.instance.db.MastodonInstanceInfoRecord
import net.pantasystem.milktea.data.infrastructure.instance.db.MetaDAO
import net.pantasystem.milktea.data.infrastructure.instance.db.MetaDTO
import net.pantasystem.milktea.data.infrastructure.instance.db.PleromaMetadataFeatures
import net.pantasystem.milktea.data.infrastructure.instance.ticker.db.InstanceTickerDAO
import net.pantasystem.milktea.data.infrastructure.instance.ticker.db.InstanceTickerRecord
import net.pantasystem.milktea.data.infrastructure.list.UserListDao
import net.pantasystem.milktea.data.infrastructure.list.UserListMemberIdRecord
import net.pantasystem.milktea.data.infrastructure.list.UserListMemberView
import net.pantasystem.milktea.data.infrastructure.list.UserListRecord
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoDao
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoRecord
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftFileJunctionRef
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftLocalFile
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftNoteDTO
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.note.draft.db.PollChoiceDTO
import net.pantasystem.milktea.data.infrastructure.note.draft.db.UserIdDTO
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.MastodonMentionEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.MastodonTagEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteAncestorEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteCustomEmojiEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteDAO
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteDescendantEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteFileEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NotePollChoiceEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteThreadDAO
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteThreadEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteVisibleUserIdEntity
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.ReactionCountEntity
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.history.ReactionHistoryDao
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.history.ReactionHistoryRecord
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.data.infrastructure.note.timeline.TimelineCacheDAO
import net.pantasystem.milktea.data.infrastructure.note.timeline.TimelineItemEntity
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
import net.pantasystem.milktea.data.infrastructure.user.db.BadgeRoleRecord
import net.pantasystem.milktea.data.infrastructure.user.db.PinnedNoteIdRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserDao
import net.pantasystem.milktea.data.infrastructure.user.db.UserDetailedStateRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserEmojiRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserInfoStateRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserInstanceInfoRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserProfileFieldRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserRelatedStateRecord
import net.pantasystem.milktea.data.infrastructure.user.db.UserView
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord

@Database(
    entities = [
        ReactionHistoryRecord::class,
        ReactionUserSetting::class,
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
        BadgeRoleRecord::class,

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

        InstanceTickerRecord::class,

        NoteEntity::class,
        ReactionCountEntity::class,
        NoteVisibleUserIdEntity::class,
        NotePollChoiceEntity::class,
        MastodonTagEntity::class,
        MastodonMentionEntity::class,
        NoteCustomEmojiEntity::class,
        NoteFileEntity::class,

        NoteThreadEntity::class,
        NoteAncestorEntity::class,
        NoteDescendantEntity::class,

        TimelineItemEntity::class,
    ],
    version = 64,
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
        AutoMigration(from = 56, to = 57),
        AutoMigration(from = 58, to = 59),
        AutoMigration(from = 59, to = 60),
        AutoMigration(from = 60, to = 61),
        AutoMigration(from = 61, to = 62),
        AutoMigration(from = 62, to = 63),
        AutoMigration(from = 63, to = 64),
    ],
    views = [UserView::class, GroupMemberView::class, UserListMemberView::class]
)
@TypeConverters(
    DateConverter::class,
    TimelinePageTypeConverter::class,
    AccountInstanceTypeConverter::class,
    InstantConverter::class,
    LocalDateConverter::class,
)
abstract class DataBase : RoomDatabase() {
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao

    abstract fun reactionHistoryDao(): ReactionHistoryDao
    abstract fun reactionUserSettingDao(): ReactionUserSettingDao

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

    abstract fun instanceTickerDAO(): InstanceTickerDAO

    abstract fun noteDAO(): NoteDAO

    abstract fun noteThreadDAO(): NoteThreadDAO

    abstract fun timelineCacheDAO(): TimelineCacheDAO
}