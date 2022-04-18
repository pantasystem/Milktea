package jp.panta.misskeyandroidclient.viewmodel.explore

import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.SortedUsersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.api.misskey.users.RequestUser
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