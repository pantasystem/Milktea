package jp.panta.misskeyandroidclient.viewmodel

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.messaging.MessageStreamFilter
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStore
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationSubscribeViewModel
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserRepositoryEventToFlow
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MiCore{
    //val accounts: MutableLiveData<List<Account>>

    //val currentAccount: MutableLiveData<Account>

    var notificationSubscribeViewModel: NotificationSubscribeViewModel

    var messageStreamFilter: MessageStreamFilter

    val loggerFactory: Logger.Factory

    fun getAccounts(): StateFlow<List<Account>>

    fun getCurrentAccount(): StateFlow<Account?>

    @Throws(AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long) : Account

    fun getAccountRepository(): AccountRepository

    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

    fun getNoteRepository(): NoteRepository

    fun getUserRepository(): UserRepository

    fun getUserRepositoryEventToFlow(): UserRepositoryEventToFlow


    fun setCurrentAccount(account: Account)

    fun logoutAccount(account: Account)

    fun addAccount(account: Account)

    fun addPageInCurrentAccount(page: Page)

    fun replaceAllPagesInCurrentAccount(pages: List<Page>)

    fun removePageInCurrentAccount(page: Page)

    fun removeAllPagesInCurrentAccount(pages: List<Page>)


    fun getMisskeyAPI(instanceDomain: String): MisskeyAPI


    fun getMisskeyAPI(account: Account): MisskeyAPI

    fun getEncryption(): Encryption


    fun getChannelAPI(account: Account): ChannelAPI

    fun getNoteCaptureAdapter() : NoteCaptureAPIAdapter

    fun getCurrentInstanceMeta(): Meta?



    fun getSettingStore(): SettingStore



}