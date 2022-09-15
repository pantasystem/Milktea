package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.userlist.UserListStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User

@FlowPreview
@ExperimentalCoroutinesApi
class UserListDetailViewModel @AssistedInject constructor(
    private val userListStore: UserListStore,
    private val userViewDataFactory: UserViewData.Factory,
    loggerFactory: Logger.Factory,
    @Assisted val listId: UserList.Id,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(listId: UserList.Id): UserListDetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: ViewModelAssistedFactory,
            listId: UserList.Id
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
            userViewDataFactory.create(id, viewModelScope)
        }
    }.asLiveData()

    private val logger = loggerFactory.create("UserListDetailViewModel")

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

    fun pushUser(userId: User.Id) {

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


    fun pullUser(userId: User.Id) {

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListStore.removeUser(listId, userId)
            }.onFailure { t ->
                logger.warning("ユーザーの除去に失敗", e = t)
            }.onSuccess {
                logger.info("ユーザーの除去に成功")
            }
        }


    }

}