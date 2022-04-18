package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.data.gettters.Getters

import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.notes.NoteCaptureAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.model.drive.FileUploaderProvider
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.model.messaging.impl.MessageObserver
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.model.settings.SettingStore
import net.pantasystem.milktea.data.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.model.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.data.model.url.UrlPreviewStore

interface MiCore {


    @ExperimentalCoroutinesApi
    @FlowPreview
    val messageObserver: MessageObserver

    val loggerFactory: net.pantasystem.milktea.common.Logger.Factory


    @Throws(net.pantasystem.milktea.model.account.AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long): net.pantasystem.milktea.model.account.Account

    fun getAccountRepository(): net.pantasystem.milktea.model.account.AccountRepository

    fun getUrlPreviewStore(account: net.pantasystem.milktea.model.account.Account): UrlPreviewStore?

    fun getNoteDataSource(): net.pantasystem.milktea.model.notes.NoteDataSource

    fun getNoteRepository(): net.pantasystem.milktea.model.notes.NoteRepository

    fun getUserDataSource(): net.pantasystem.milktea.model.user.UserDataSource

    fun getUserRepository(): net.pantasystem.milktea.model.user.UserRepository

    fun getNotificationDataSource(): net.pantasystem.milktea.model.notification.NotificationDataSource

    fun getNotificationRepository(): net.pantasystem.milktea.model.notification.NotificationRepository

    fun getUserRepositoryEventToFlow(): net.pantasystem.milktea.model.user.UserRepositoryEventToFlow

    fun getGroupDataSource(): net.pantasystem.milktea.model.group.GroupDataSource

    fun getGroupRepository(): net.pantasystem.milktea.model.group.GroupRepository

    fun getFilePropertyDataSource(): net.pantasystem.milktea.model.drive.FilePropertyDataSource

    fun getDriveFileRepository(): net.pantasystem.milktea.model.drive.DriveFileRepository

    fun getSubscriptionRegistration(): SubscriptionRegistration

    fun getSubscriptionUnRegstration(): SubscriptionUnRegistration

    suspend fun setCurrentAccount(account: net.pantasystem.milktea.model.account.Account)


    fun getEncryption(): Encryption


    suspend fun getChannelAPI(account: net.pantasystem.milktea.model.account.Account): ChannelAPI

    fun getNoteCaptureAdapter(): NoteCaptureAPIAdapter

    fun getSocket(account: net.pantasystem.milktea.model.account.Account): Socket

    fun getNoteCaptureAPI(account: net.pantasystem.milktea.model.account.Account): NoteCaptureAPI

    fun getCurrentInstanceMeta(): net.pantasystem.milktea.model.instance.Meta?


    fun getSettingStore(): SettingStore

    fun getGetters(): Getters

    fun getMessageRepository(): net.pantasystem.milktea.model.messaging.MessageRepository

    fun getMessageDataSource(): MessageDataSource

    fun getUnreadMessages(): net.pantasystem.milktea.model.messaging.UnReadMessages


    fun getDraftNoteDAO(): net.pantasystem.milktea.model.notes.draft.DraftNoteDao

    fun getUnreadNotificationDAO(): UnreadNotificationDAO

    fun getMisskeyAPIProvider(): MisskeyAPIProvider

    fun getMetaStore(): net.pantasystem.milktea.model.instance.FetchMeta

    fun getMetaRepository(): net.pantasystem.milktea.model.instance.MetaRepository

    fun getReactionHistoryPaginatorFactory(): net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator.Factory

    fun getReactionHistoryDataSource(): net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource

    fun getGalleryDataSource(): net.pantasystem.milktea.model.gallery.GalleryDataSource

    fun getGalleryRepository(): net.pantasystem.milktea.model.gallery.GalleryRepository

    fun getFileUploaderProvider(): FileUploaderProvider

    fun getTranslationStore(): net.pantasystem.milktea.model.notes.NoteTranslationStore

    fun getNoteReservationPostExecutor(): net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor

    fun getAccountStore(): net.pantasystem.milktea.model.account.AccountStore
}