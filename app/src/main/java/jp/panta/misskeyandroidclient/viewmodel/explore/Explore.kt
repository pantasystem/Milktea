package jp.panta.misskeyandroidclient.viewmodel.explore

import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.SortedUsersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.io.Serializable

sealed class Explore(open val name: String): Serializable{

    data class Tag(
        override val name: String,
        val conditions: SortedHashTagListViewModel.Conditions
    ): Explore(name)

    data class User(
        override val name: String,
        val origin: RequestUser.Origin?,
        val sort: String?,
        val state: RequestUser.State?
    ): Explore(name)

    @FlowPreview
    @ExperimentalCoroutinesApi
    data class UserType(
        override val name: String,
        val type: SortedUsersViewModel.Type
    ): Explore(name)
}