package jp.panta.misskeyandroidclient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.Observer
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteCapture
import jp.panta.misskeyandroidclient.model.messaging.MessageSubscriber
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStore
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationSubscribeViewModel
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Page

interface MiCore{
    //val accounts: MutableLiveData<List<Account>>

    //val currentAccount: MutableLiveData<Account>

    var notificationSubscribeViewModel: NotificationSubscribeViewModel

    var messageSubscriber: MessageSubscriber


    fun getAccounts(): LiveData<List<Account>>

    fun getCurrentAccount(): LiveData<Account>

    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

    fun setCurrentAccount(account: Account)

    fun logoutAccount(account: Account)

    fun addAccount(account: Account)

    fun addPageInCurrentAccount(page: Page)

    fun replaceAllPagesInCurrentAccount(pages: List<Page>)

    fun removePageInCurrentAccount(page: Page)

    fun removeAllPagesInCurrentAccount(pages: List<Page>)




    fun getMisskeyAPI(account: Account): MisskeyAPI

    fun getEncryption(): Encryption

    fun getMainCapture(account: Account): MainCapture

    fun setupObserver(account: Account, observer: Observer)

    fun getNoteCapture(account: Account): NoteCapture

    fun getCurrentInstanceMeta(): Meta?


    fun getTimelineCapture(account: Account): TimelineCapture

    fun getSettingStore(): SettingStore


}