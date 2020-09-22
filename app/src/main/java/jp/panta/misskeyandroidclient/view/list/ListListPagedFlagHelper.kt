package jp.panta.misskeyandroidclient.view.list

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.list.UserList


object ListListPagedFlagHelper {

    @JvmStatic
    @BindingAdapter("list", "pagedList")
    fun ImageButton.setListListTogglePageIcon(list: UserList?, pagedList: Set<UserList>?){
        list?: return
        pagedList?: return
        val isPaged = pagedList.contains(list)
        if(isPaged){
            this.setImageResource(R.drawable.ic_remove_to_tab_24px)
        }else{
            this.setImageResource(R.drawable.ic_add_to_tab_24px)
        }

    }
}