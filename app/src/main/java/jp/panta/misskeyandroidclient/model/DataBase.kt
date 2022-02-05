package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.core.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notes.draft.db.DraftFileDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.DraftNoteDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.PollChoiceDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.UserIdDTO
import jp.panta.misskeyandroidclient.model.url.UrlPreview
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.db.AccountDAO
import jp.panta.misskeyandroidclient.model.account.page.TimelinePageTypeConverter
import jp.panta.misskeyandroidclient.model.account.page.db.PageDAO
import jp.panta.misskeyandroidclient.model.instance.db.*
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSetting
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotification
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.users.impl.UserNicknameDAO
import jp.panta.misskeyandroidclient.model.users.impl.UserNicknameDTO

@Database(
    entities = [
        EncryptedConnectionInformation::class,
        ReactionHistory::class,
        jp.panta.misskeyandroidclient.model.core.Account::class,
        ReactionUserSetting::class,
        jp.panta.misskeyandroidclient.model.Page::class,
        PollChoiceDTO::class,
        UserIdDTO::class,
        DraftFileDTO::class,
        DraftNoteDTO::class,

        UrlPreview::class,
        Account::class,
        Page::class,
        MetaDTO::class,
        EmojiDTO::class,
        EmojiAlias::class,
        UnreadNotification::class,
        UserNicknameDTO::class,
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(PageTypeConverter::class, DateConverter::class, TimelinePageTypeConverter::class)
abstract class DataBase : RoomDatabase(){
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