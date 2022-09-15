package net.pantasystem.milktea.userlist

import android.util.Log
import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.list.UserList


object ListListPagedFlagHelper {

    @JvmStatic
    @BindingAdapter("list", "pagedList")
    fun ImageButton.setListListTogglePageIcon(list: UserList?, pagedList: Set<UserList>?){
        if(list == null){
            Log.d("ListListPagedFlagHelper", "UserList is null")
            return
        }
        val isPaged = pagedList?.contains(list) == true
        if(isPaged){
            this.setImageResource(R.drawable.ic_remove_to_tab_24px)
        }else{
            this.setImageResource(R.drawable.ic_add_to_tab_24px)
        }

    }
}