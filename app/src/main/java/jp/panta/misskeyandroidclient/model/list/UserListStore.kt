package jp.panta.misskeyandroidclient.model.list

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserListStore @Inject constructor(
    val userListRepository: UserListRepository
) {
    private val _state: MutableStateFlow<UserListState> =
        MutableStateFlow(UserListState(emptyMap()))

    val state: StateFlow<UserListState> = _state

    suspend fun findByAccount(accountId: Long): List<UserList> {
        val lists = userListRepository.findByAccountId(accountId)
        _state.value = _state.value.replaceAll(accountId, lists)
        return lists
    }

    suspend fun create(accountId: Long, name: String) {
        val result = userListRepository.create(accountId, name)
        _state.value = _state.value.created(result)
    }

    suspend fun update(id: UserList.Id, name: String) {
        userListRepository.update(id, name)
        val list = state.value.get(id)
        if (list == null) {
            findByAccount(id.accountId)
        } else {
            _state.value = _state.value.updated(list.copy(name = name))
        }
    }

    suspend fun delete(id: UserList.Id) {
        userListRepository.delete(id)
        _state.value = _state.value.deleted(id)
    }
}