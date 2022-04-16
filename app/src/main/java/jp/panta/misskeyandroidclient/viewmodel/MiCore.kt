package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.data.gettters.Getters

import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.notes.NoteCaptureAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.model.Encryption
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.account.AccountNotFoundException
import net.pantasystem.milktea.data.model.account.AccountRepository
import net.pantasystem.milktea.data.model.account.AccountStore
import net.pantasystem.milktea.data.model.drive.DriveFileRepository
import net.pantasystem.milktea.data.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.data.model.drive.FileUploaderProvider
import net.pantasystem.milktea.data.model.gallery.GalleryDataSource
import net.pantasystem.milktea.data.model.gallery.GalleryRepository
import net.pantasystem.milktea.data.model.group.GroupDataSource
import net.pantasystem.milktea.data.model.group.GroupRepository
import net.pantasystem.milktea.data.model.instance.FetchMeta
import net.pantasystem.milktea.data.model.instance.Meta
import net.pantasystem.milktea.data.model.instance.MetaRepository
import net.pantasystem.milktea.data.model.messaging.MessageObserver
import net.pantasystem.milktea.data.model.messaging.MessageRepository
import net.pantasystem.milktea.data.model.messaging.UnReadMessages
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.data.model.notes.NoteDataSource
import net.pantasystem.milktea.data.model.notes.NoteRepository
import net.pantasystem.milktea.data.model.notes.NoteTranslationStore
import net.pantasystem.milktea.data.model.notes.draft.DraftNoteDao
import net.pantasystem.milktea.data.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.data.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.data.model.notes.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.data.model.notification.NotificationDataSource
import net.pantasystem.milktea.data.model.notification.NotificationRepository
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.model.settings.SettingStore
import net.pantasystem.milktea.data.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.model.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.data.model.url.UrlPreviewStore
import net.pantasystem.milktea.data.model.users.UserDataSource
import net.pantasystem.milktea.data.model.users.UserRepository
import net.pantasystem.milktea.data.model.users.UserRepositoryEventToFlow

interface MiCore {


    @ExperimentalCoroutinesApi
    @FlowPreview
    val messageObserver: MessageObserver

    val loggerFactory: net.pantasystem.milktea.common.Logger.Factory


    @Throws(AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long): Account

    fun getAccountRepository(): AccountRepository

    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

    fun getNoteDataSource(): NoteDataSource

    fun getNoteRepository(): NoteRepository

    fun getUserDataSource(): UserDataSource

    fun getUserRepository(): UserRepository

    fun getNotificationDataSource(): NotificationDataSource

    fun getNotificationRepository(): NotificationRepository

    fun getUserRepositoryEventToFlow(): UserRepositoryEventToFlow

    fun getGroupDataSource(): GroupDataSource

    fun getGroupRepository(): GroupRepository

    fun getFilePropertyDataSource(): FilePropertyDataSource

    fun getDriveFileRepository(): DriveFileRepository

    fun getSubscriptionRegistration(): SubscriptionRegistration

    fun getSubscriptionUnRegstration(): SubscriptionUnRegistration

    suspend fun setCurrentAccount(account: Account)


    fun getEncryption(): Encryption


    suspend fun getChannelAPI(account: Account): ChannelAPI

    fun getNoteCaptureAdapter(): NoteCaptureAPIAdapter

    fun getSocket(account: Account): Socket

    fun getNoteCaptureAPI(account: Account): NoteCaptureAPI

    fun getCurrentInstanceMeta(): Meta?


    fun getSettingStore(): SettingStore

    fun getGetters(): Getters

    fun getMessageRepository(): MessageRepository

    fun getMessageDataSource(): MessageDataSource

    fun getUnreadMessages(): UnReadMessages


    fun getDraftNoteDAO(): DraftNoteDao

    fun getUnreadNotificationDAO(): UnreadNotificationDAO

    fun getMisskeyAPIProvider(): MisskeyAPIProvider

    fun getMetaStore(): FetchMeta

    fun getMetaRepository(): MetaRepository

    fun getReactionHistoryPaginatorFactory(): ReactionHistoryPaginator.Factory

    fun getReactionHistoryDataSource(): ReactionHistoryDataSource

    fun getGalleryDataSource(): GalleryDataSource

    fun getGalleryRepository(): GalleryRepository

    fun getFileUploaderProvider(): FileUploaderProvider

    fun getTranslationStore(): NoteTranslationStore

    fun getNoteReservationPostExecutor(): NoteReservationPostExecutor

    fun getAccountStore(): AccountStore
}