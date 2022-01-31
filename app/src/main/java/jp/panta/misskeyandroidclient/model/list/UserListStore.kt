package jp.panta.misskeyandroidclient.model.list

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserListStore @Inject constructor() {
    private val _state: MutableStateFlow<UserListState> =
        MutableStateFlow(UserListState(emptyMap()))



}