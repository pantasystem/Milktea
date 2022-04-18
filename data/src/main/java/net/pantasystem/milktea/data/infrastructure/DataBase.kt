package net.pantasystem.milktea.data.infrastructure

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.pantasystem.milktea.data.infrastructure.core.*
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftFileDTO
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDTO
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.PollChoiceDTO
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.UserIdDTO
import net.pantasystem.milktea.data.infrastructure.url.UrlPreview
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.infrastructure.account.db.AccountDAO
import net.pantasystem.milktea.data.infrastructure.account.page.db.PageDAO
import net.pantasystem.milktea.data.infrastructure.account.page.db.TimelinePageTypeConverter
import net.pantasystem.milktea.api.Instance.db.*
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSetting
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotification
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.user.impl.UserNicknameDAO
import net.pantasystem.milktea.data.infrastructure.user.impl.UserNicknameDTO
import net.pantasystem.milktea.model.account.AccountInstanceTypeConverter

@Database(
    entities = [
        EncryptedConnectionInformation::class,
        ReactionHistory::class,
        Account::class,
        ReactionUserSetting::class,
        Page::class,
        PollChoiceDTO::class,
        UserIdDTO::class,
        DraftFileDTO::class,
        DraftNoteDTO::class,

        UrlPreview::class,
        net.pantasystem.milktea.model.account.Account::class,
        net.pantasystem.milktea.model.account.page.Page::class,
        MetaDTO::class,
        EmojiDTO::class,
        EmojiAlias::class,
        UnreadNotification::class,
        UserNicknameDTO::class,
    ],
    version = 12,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 11, to = 12)
    ]
)
@TypeConverters(
    PageTypeConverter::class,
    DateConverter::class,
    TimelinePageTypeConverter::class,
    AccountInstanceTypeConverter::class,
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
    abstract fun emojiAliasDAO(): EmojiAliasDAO
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao

    abstract fun unreadNotificationDAO(): UnreadNotificationDAO

    abstract fun userNicknameDAO(): UserNicknameDAO
}