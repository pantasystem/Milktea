package jp.panta.misskeyandroidclient.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.Observer
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.note.NoteCapture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

interface MiCore{
    val accounts: MutableLiveData<List<AccountRelation>>

    val currentAccount: MutableLiveData<AccountRelation>

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


}