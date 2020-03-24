package jp.panta.misskeyandroidclient.view.list

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.list.UserList


object ListListPagedFlagHelper {

    @JvmStatic
    @BindingAdapter("list", "pagedList")
    fun ImageButton.setListListTogglePageIcon(list: UserList?, pagedList: List<UserList>?){
        list?: return
        pagedList?: return
        val isPaged = pagedList.any {
            it.id == list.id
        }
        if(isPaged){
            this.setImageResource(R.drawable.ic_remove_black_24dp)
        }else{
            this.setImageResource(R.drawable.ic_add_black_24dp)
        }

    }
}