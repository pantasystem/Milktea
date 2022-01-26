package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.MediatorLiveData
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@Deprecated("UserViewDataが今までの機能を完全に引き継いだため不要になった")
open class UsersLiveData(
    val miCore: MiCore,
) : MediatorLiveData<List<UserViewData>>()