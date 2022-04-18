package jp.panta.misskeyandroidclient.ui.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListStore
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class UserListDetailViewModel @AssistedInject constructor(
    val miCore: MiCore,
    val userListStore: net.pantasystem.milktea.model.list.UserListStore,
    @Assisted val listId: net.pantasystem.milktea.model.list.UserList.Id,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(listId: net.pantasystem.milktea.model.list.UserList.Id): UserListDetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: ViewModelAssistedFactory,
            listId: net.pantasystem.milktea.model.list.UserList.Id
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(listId) as T
            }
        }
    }


    val userList = userListStore.state.map {
        it.get(listId)
    }.filterNotNull().asLiveData()


    val listUsers = userListStore.state.map {
        it.get(listId)
    }.filterNotNull().map {
        it.userIds.map { id ->
            UserViewData(id, miCore, viewModelScope)
        }
    }.asLiveData()

    private val logger = miCore.loggerFactory.create("UserListDetailViewModel")

    init {
        load()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListStore.findOne(listId)
            }.onSuccess {
                logger.info("load list success")
            }.onFailure {
                logger.error("load list error", e = it)
            }

        }

    }

    fun updateName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListStore.update(listId, name)
            }.onSuccess {
                load()
            }.onFailure { t ->
                logger.error("名前の更新に失敗した", e = t)
            }
        }

    }

    fun pushUser(userId: net.pantasystem.milktea.model.user.User.Id) {

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListStore.appendUser(listId, userId)
            }.onSuccess {
                logger.info("ユーザーの追加に成功")
            }.onFailure {
                logger.warning("ユーザーの追加に失敗", e = it)
            }
        }

    }


    fun pullUser(userId: net.pantasystem.milktea.model.user.User.Id) {

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListStore.removeUser(listId, userId)
            }.onFailure { t ->
                logger.warning("ユーザーの除去に失敗")
            }.onSuccess {
                logger.info("ユーザーの除去に成功")
            }
        }


    }

}