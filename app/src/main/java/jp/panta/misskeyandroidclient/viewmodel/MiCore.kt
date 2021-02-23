package jp.panta.misskeyandroidclient.viewmodel

import androidx.lifecycle.LiveData
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.Observer
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.v2.NoteCapture
import jp.panta.misskeyandroidclient.model.messaging.MessageSubscriber
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStore
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationSubscribeViewModel
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.notes.NoteEventStore
import jp.panta.misskeyandroidclient.model.notes.NoteRepository

interface MiCore{
    //val accounts: MutableLiveData<List<Account>>

    //val currentAccount: MutableLiveData<Account>

    var notificationSubscribeViewModel: NotificationSubscribeViewModel

    var messageSubscriber: MessageSubscriber

    val loggerFactory: Logger.Factory

    fun getAccounts(): LiveData<List<Account>>

    fun getCurrentAccount(): LiveData<Account>

    @Throws(AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long) : Account

    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

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

    fun getMainCapture(account: Account): MainCapture

    fun setupObserver(account: Account, observer: Observer)

    fun getNoteCapture(account: Account): NoteCapture

    fun getCurrentInstanceMeta(): Meta?


    fun getTimelineCapture(account: Account): TimelineCapture

    fun getSettingStore(): SettingStore


    fun getNoteEventStore(account: Account) : NoteEventStore

}