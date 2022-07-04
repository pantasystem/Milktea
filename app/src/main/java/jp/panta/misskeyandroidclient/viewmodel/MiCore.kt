package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository

interface MiCore {


    val loggerFactory: Logger.Factory


    @Throws(AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long): Account


    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

    fun getNoteDataSource(): NoteDataSource


    fun getUserDataSource(): UserDataSource

    fun getUserRepository(): UserRepository


    fun getGroupRepository(): GroupRepository

    fun getFilePropertyDataSource(): FilePropertyDataSource


    fun getSubscriptionRegistration(): SubscriptionRegistration

    fun getSubscriptionUnRegstration(): SubscriptionUnRegistration


    fun getEncryption(): Encryption


    fun getCurrentInstanceMeta(): Meta?


    fun getSettingStore(): SettingStore

    fun getGetters(): Getters


    fun getMisskeyAPIProvider(): MisskeyAPIProvider


    fun getMetaRepository(): MetaRepository

    fun getReactionHistoryPaginatorFactory(): ReactionHistoryPaginator.Factory

    fun getReactionHistoryDataSource(): ReactionHistoryDataSource


    fun getNoteReservationPostExecutor(): NoteReservationPostExecutor

    fun getAccountStore(): AccountStore
}