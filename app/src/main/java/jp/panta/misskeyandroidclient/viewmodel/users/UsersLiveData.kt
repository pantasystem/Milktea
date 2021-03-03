package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.MediatorLiveData
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


@Deprecated("UserViewDataが今までの機能を完全に引き継いだため不要になった")
open class UsersLiveData(
    val miCore: MiCore,
) : MediatorLiveData<List<UserViewData>>()