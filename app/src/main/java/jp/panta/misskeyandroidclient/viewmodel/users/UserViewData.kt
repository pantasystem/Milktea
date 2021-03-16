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

    private var mUser: User.Detail? = null
        set(value) {
            field = value
            user.postValue(value)
        }

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
                        mUser = detail
                    }
                }
                is UserDataSource.Event.Updated -> {
                    (it.user as? User.Detail)?.let { detail ->
                        mUser = detail
                    }
                }
                is UserDataSource.Event.Removed -> {
                    mUser = null
                }
            }
        }.launchIn(coroutineScope + dispatcher)

        coroutineScope.launch(dispatcher) {
            initLoad()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun initLoad() {

        if(user.value == null){
            val u : User.Detail? = runCatching {
                miCore.getUserRepository().find(userId, true)
            }.getOrNull() as? User.Detail

            u?.let{
                mUser = it
            }
        }
    }



}