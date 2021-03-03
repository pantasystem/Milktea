package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserRepositoryEventToFlow
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserViewData(
    val userId: User.Id,
    val miCore: MiCore,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
){

    val user: MutableLiveData<User?> = MutableLiveData<User?>()

    constructor(
        user: User,
        miCore: MiCore,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): this(user.id, miCore, coroutineScope, dispatcher){
        this.user.postValue(user)
    }

    init {
        miCore.getUserRepositoryEventToFlow().from(userId).onEach {
            when(it) {
                is UserRepository.Event.Created -> {
                    user.postValue(it.user)
                }
                is UserRepository.Event.Updated -> {
                    user.postValue(it.user)
                }
                is UserRepository.Event.Removed -> {
                    user.postValue(null)
                }
            }
        }.launchIn(coroutineScope + dispatcher)

        coroutineScope.launch(dispatcher) {
            initLoad()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun initLoad() {
        val account = runCatching {
            miCore.getAccountRepository().get(userId.accountId)
        }.getOrNull()
            ?:return
        if(user.value == null){
            var u : User? = runCatching {
                miCore.getUserRepository().get(userId)
            }.getOrNull()

            if(u != null) {
                user.postValue(u)
                return
            }

            val i = runCatching {
                account.getI(miCore.getEncryption())
            }.getOrNull()
            i?: return

            val res = miCore.getMisskeyAPI(account).showUser(RequestUser(
                i = i,
                userId = userId.id,
                detail = true
            )).execute()

            val dto = res.body()
            u = dto?.toUser(account, true)
            if(u != null){
                miCore.getUserRepository().add(u)
            }
        }
    }


}