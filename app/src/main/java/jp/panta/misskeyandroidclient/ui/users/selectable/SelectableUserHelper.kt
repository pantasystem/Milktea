package jp.panta.misskeyandroidclient.ui.users.selectable

import android.view.View
import android.widget.CheckBox
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.user.User

object SelectableUserHelper {

    @JvmStatic
    @BindingAdapter("selectedUsers", "selectableUserEnabled", "selectableMaxCount")
    fun CheckBox.setSelectableUserEnabled(selectedUsers: Set<net.pantasystem.milktea.model.user.User.Id>?, selectableUserEnabled: net.pantasystem.milktea.model.user.User?, selectableMaxCount: Int?){
        val max = selectableMaxCount?: 0
        if(selectableUserEnabled == null || max <= 0){
            this.visibility = View.GONE
            return
        }
        val selected = selectedUsers?: emptySet()
        this.visibility = View.VISIBLE
        this.isEnabled = selected.contains(selectableUserEnabled.id) || selected.size < max
    }
}