package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.user.User
import java.io.Serializable

interface SearchAndSelectUserNavigation : ActivityNavigation<SearchAndSelectUserNavigationArgs> {
    companion object {
        const val EXTRA_SELECTABLE_MAXIMUM_SIZE =
            "jp.panta.misskeyandroidclient.EXTRA_SELECTABLE_MAXIMUM_SIZE"
        const val EXTRA_SELECTED_USER_IDS =
            "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_IDS"

        const val EXTRA_SELECTED_USER_CHANGED_DIFF =
            "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_CHANGED_DIFF"
    }

}

data class SearchAndSelectUserNavigationArgs(
    val selectableMaximumSize: Int = Int.MAX_VALUE,
    val selectedUserIds: List<User.Id> = emptyList()
)

data class ChangedDiffResult(
    val selected: List<User.Id>,
    val added: List<User.Id>,
    val removed: List<User.Id>,
    val selectedUserNames: List<String>,
) : Serializable
