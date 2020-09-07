package jp.panta.misskeyandroidclient.viewmodel

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.Observer
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteCapture
import jp.panta.misskeyandroidclient.model.messaging.MessageSubscriber
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStore
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationSubscribeViewModel

interface MiCore{
    val accounts: MutableLiveData<List<AccountRelation>>

    val currentAccount: MutableLiveData<AccountRelation>

    var notificationSubscribeViewModel: NotificationSubscribeViewModel

    var messageSubscriber: MessageSubscriber

    val urlPreviewStore: UrlPreviewStore?

    fun getUrlPreviewStore(account: AccountRelation?): UrlPreviewStore?

    fun switchAccount(account: Account)

    fun logoutAccount(account: Account)

    fun removeConnectSetting(connectionInformation: EncryptedConnectionInformation)

    fun addPageInCurrentAccount(page: Page)

    fun replaceAllPagesInCurrentAccount(pages: List<Page>)

    fun removePageInCurrentAccount(page: Page)

    fun removeAllPagesInCurrentAccount(pages: List<Page>)

    fun putConnectionInfo(account: Account, ci: EncryptedConnectionInformation)

    fun removeConnectionInfoInCurrentAccount(ci: EncryptedConnectionInformation)

    fun getMisskeyAPI(ci: EncryptedConnectionInformation): MisskeyAPI

    fun getMisskeyAPI(accountRelation: AccountRelation?): MisskeyAPI?

    fun getEncryption(): Encryption

    fun getMainCapture(account: AccountRelation): MainCapture

    fun setupObserver(account: AccountRelation, observer: Observer)

    fun getNoteCapture(account: AccountRelation): NoteCapture

    fun getCurrentInstanceMeta(): Meta?

    fun getStreamingAdapter(account: AccountRelation): StreamingAdapter

    fun getTimelineCapture(account: AccountRelation): TimelineCapture

    fun getSettingStore(): SettingStore


}