package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.api.v10.MisskeyAPIV10
import jp.panta.misskeyandroidclient.api.v10.RequestFollowFollower
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.collections.ArrayList

class FollowFollowerViewModel(
    val account: Account,
    val misskeyAPI: MisskeyAPI,
    val user: UserDTO?,
    val type: Type,
    val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption()
) : ViewModel(), ShowUserDetails{
    @Suppress("UNCHECKED_CAST")
    class Factory(
        val account: Account,
        val miApplication: MiApplication,
        val user: UserDTO?,
        val type: Type,
        val encryption: Encryption
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val misskeyAPI = miApplication.getMisskeyAPI(account)
            if(modelClass == FollowFollowerViewModel::class.java){
                return FollowFollowerViewModel(
                    account,
                    misskeyAPI,
                    user,
                    type,
                    miApplication
                ) as T
            }
            throw IllegalArgumentException("use FollowFollowerViewModel::class.java")
        }
    }

    enum class Type{
        FOLLOWING,
        FOLLOWER
    }

    val tag = "FollowFollowerViewModel"

    val userId = user?.id?: account.remoteId




    val isInitializing = MutableLiveData<Boolean>(false)


    val users = MutableLiveData<List<UserViewData>>()

    private var mIsLoading: Boolean = false

    private data class Result(val nextId: String?, val users: List<UserDTO>)
    private abstract inner class Loader{
        private var result: Result? = null
        abstract fun onLoadInit(): Result?
        abstract fun onLoadNext(nextId: String?): Result?
        fun loadInit(){
            if(mIsLoading){
                return
            }
            result = null
            users.value = emptyList()
            loadNext()
        }

        fun loadNext(){
            if(mIsLoading){
                return
            }
            if(result != null && result?.nextId == null){
                return
            }
            mIsLoading = true
            viewModelScope.launch(Dispatchers.IO){

                val tmpCursorId = result?.nextId
                val beforeResult = result


                try{
                    if(beforeResult != null && beforeResult.nextId == null){
                        return@launch
                    }

                    val result = if(tmpCursorId == null){
                        onLoadInit()
                    }else{
                        onLoadNext(tmpCursorId)
                    }

                    result?: return@launch
                    this@Loader.result = result
                    val viewDataList = result.users.map{ dto ->
                        val u = dto.toUser(account)
                        miCore.getUserRepository().add(u)
                        UserViewData(u, miCore, viewModelScope)
                    }
                    val arrayList = ArrayList(users.value?: emptyList())
                    arrayList.addAll(viewDataList)
                    users.postValue(arrayList)

                }catch(e: Exception){
                    Log.e(tag, "読み込み中にエラー発生", e)
                }finally {
                    mIsLoading = false
                    if(tmpCursorId == null){
                        isInitializing.postValue(false)
                    }
                }
            }

        }
    }

    private inner class V10Loader(misskeyAPIV10: MisskeyAPIV10) : Loader(){
        val store = if(type == Type.FOLLOWING){
            misskeyAPIV10::following
        }else{
            misskeyAPIV10::followers
        }
        override fun onLoadInit(): Result? {
            return onLoadNext(null)
        }

        override fun onLoadNext(nextId: String?): Result? {
            val res = store.invoke(
                RequestFollowFollower(
                    i = account.getI(miCore.getEncryption()),
                    userId = userId,
                    cursor = nextId
                )
            ).execute()
            val body = res.body()?: return null
            Log.d(tag, "受信したデータ:$body")
            if(body.users.isNullOrEmpty()){
                Log.e(tag, "受信に失敗した: ${res.code()}, ${res.errorBody()}")
            }
            return Result(body.next, body.users)
        }

    }

    private inner class V11Loader(misskeyAPIV11: MisskeyAPIV11) : Loader(){

        val store = if(type == Type.FOLLOWING){
            misskeyAPIV11::following
        }else{
            misskeyAPIV11::followers
        }
        override fun onLoadInit(): Result? {
            return onLoadNext(null)
        }

        override fun onLoadNext(nextId: String?): Result? {
            val res = store.invoke(
                RequestUser(
                    i = account.getI(miCore.getEncryption()),
                    userId = userId,
                    untilId = nextId
                )
            ).execute()
            val body = res.body()
            if(body == null){
                Log.d(tag, "取得に失敗しました:code:${res.code()} ,${res.errorBody()?.string()}")
                return null
            }
            val next = body.lastOrNull()?.id!!
            val users = body.mapNotNull {
                it.followee ?: it.follower
            }

            return Result(next, users)
        }
    }

    private val loader = when(misskeyAPI){
        is MisskeyAPIV11 ->{
            V11Loader(misskeyAPI)
        }
        is MisskeyAPIV10 ->{
            V10Loader(misskeyAPI)
        }
        else -> {
            Log.e(tag, "想定外のバージョンで実行されたかAPIの初期化に失敗している")
            null
        }
    }
    fun loadInit(){
        loader?.loadInit()
    }



    fun loadOld(){
        loader?.loadNext()
    }


    val showUserEventBus = EventBus<User.Id>()

    override fun show(userId: User.Id) {
        showUserEventBus.event = userId
    }


}