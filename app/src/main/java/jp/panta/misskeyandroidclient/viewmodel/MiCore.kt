package jp.panta.misskeyandroidclient.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

interface MiCore{
    val accounts: MutableLiveData<List<AccountRelation>>

    val currentAccount: MutableLiveData<AccountRelation>

    fun addAndChangeAccount(account: Account)

    fun logoutAccount(account: Account)

    fun removeConnectSetting(connectionInformation: EncryptedConnectionInformation)

    fun addPageInCurrentAccount(noteRequestSetting: NoteRequest.Setting)

    fun addAllPagesInCurrentAccount(noteRequestSettings: List<NoteRequest.Setting>)

    fun removePageInCurrentAccount(noteRequestSetting: NoteRequest.Setting)

    fun removeAllPagesInCurrentAccount(noteRequestSettings: List<NoteRequest.Setting>)

    fun putConnectionInfoInCurrentAccount(ci: EncryptedConnectionInformation)

    fun removeConnectionInfoInCurrentAccount(ci: EncryptedConnectionInformation)

    fun getMisskeyAPI(ci: EncryptedConnectionInformation): MisskeyAPI

    fun getEncryption(): Encryption

}