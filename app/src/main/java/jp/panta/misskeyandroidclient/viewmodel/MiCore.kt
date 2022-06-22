package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.messaging.MessageRepository
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.NoteTranslationStore
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository

interface MiCore {



    val loggerFactory: Logger.Factory


    @Throws(AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long): Account

    fun getAccountRepository(): AccountRepository

    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

    fun getNoteDataSource(): NoteDataSource

    fun getNoteRepository(): NoteRepository

    fun getUserDataSource(): UserDataSource

    fun getUserRepository(): UserRepository


    fun getGroupRepository(): GroupRepository

    fun getFilePropertyDataSource(): FilePropertyDataSource


    fun getSubscriptionRegistration(): SubscriptionRegistration

    fun getSubscriptionUnRegstration(): SubscriptionUnRegistration


    fun getEncryption(): Encryption


    suspend fun getChannelAPI(account: Account): ChannelAPI

    fun getNoteCaptureAdapter(): NoteCaptureAPIAdapter


    fun getCurrentInstanceMeta(): Meta?


    fun getSettingStore(): SettingStore

    fun getGetters(): Getters

    fun getMessageRepository(): MessageRepository


    fun getUnreadNotificationDAO(): UnreadNotificationDAO

    fun getMisskeyAPIProvider(): MisskeyAPIProvider


    fun getMetaRepository(): MetaRepository

    fun getReactionHistoryPaginatorFactory(): ReactionHistoryPaginator.Factory

    fun getReactionHistoryDataSource(): ReactionHistoryDataSource


    fun getTranslationStore(): NoteTranslationStore

    fun getNoteReservationPostExecutor(): NoteReservationPostExecutor

    fun getAccountStore(): AccountStore
}