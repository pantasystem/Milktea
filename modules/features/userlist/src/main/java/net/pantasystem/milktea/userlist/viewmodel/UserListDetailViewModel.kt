package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.user.User

class UserListDetailViewModel @AssistedInject constructor(
    private val userViewDataFactory: UserViewData.Factory,
    private val userListRepository: UserListRepository,
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



    val userList = userListRepository.observeOne(listId).filterNotNull().map {
        it.userList
    }.asLiveData()


    val listUsers = userListRepository.observeOne(listId).filterNotNull().map {
        it.userList.userIds.map { id ->
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
                userListRepository.syncOne(listId)
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
                userListRepository.update(listId, name)
                userListRepository.syncOne(listId).getOrThrow()
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
                userListRepository.appendUser(listId, userId)
                userListRepository.syncOne(listId).getOrThrow()
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
                userListRepository.removeUser(listId, userId)
                userListRepository.syncOne(listId).getOrThrow()
            }.onFailure { t ->
                logger.warning("ユーザーの除去に失敗", e = t)
            }.onSuccess {
                logger.info("ユーザーの除去に成功")
            }
        }


    }

}