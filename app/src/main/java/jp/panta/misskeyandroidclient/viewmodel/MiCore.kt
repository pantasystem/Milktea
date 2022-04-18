package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.data.gettters.Getters

import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.notes.NoteCaptureAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.drive.FileUploaderProvider
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageObserver
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryRepository
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.messaging.MessageRepository
import net.pantasystem.milktea.model.messaging.UnReadMessages
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.NoteTranslationStore
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationRepository
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.UserRepositoryEventToFlow

interface MiCore {


    @ExperimentalCoroutinesApi
    @FlowPreview
    val messageObserver: MessageObserver

    val loggerFactory: Logger.Factory


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