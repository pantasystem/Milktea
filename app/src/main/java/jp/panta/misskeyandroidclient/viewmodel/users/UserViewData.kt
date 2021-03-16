package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

open class UserViewData(
    val userId: User.Id,
    val miCore: MiCore,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
){

    val user: MutableLiveData<User.Detail?> = MutableLiveData<User.Detail?>()

    constructor(
        user: User,
        miCore: MiCore,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): this(user.id, miCore, coroutineScope, dispatcher)

    init {
        miCore.getUserRepositoryEventToFlow().from(userId).onEach {
            when(it) {
                is UserDataSource.Event.Created -> {
                    (it.user as? User.Detail)?.let{ detail ->
                        tryPost(detail)
                    }
                }
                is UserDataSource.Event.Updated -> {
                    (it.user as? User.Detail)?.let { detail ->
                        tryPost(detail)
                    }
                }
                is UserDataSource.Event.Removed -> {
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
            var u : User.Detail? = runCatching {
                miCore.getUserRepository().get(userId)
            }.getOrNull() as? User.Detail

            if(u != null) {
                tryPost(u)
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
            u = dto?.toUser(account, true) as? User.Detail
            if(u != null){
                miCore.getUserRepository().add(u)
            }
            dto?.pinnedNotes?.map { nDto ->
                nDto.toEntities(account)
            }?.forEach {
                miCore.getUserRepository().addAll(it.third)
                miCore.getNoteDataSource().addAll(it.second)
            }
            u?.let{
                tryPost(u)
            }
        }
    }

    private fun tryPost(user: User.Detail) {
        this.user.postValue(user)
    }


}