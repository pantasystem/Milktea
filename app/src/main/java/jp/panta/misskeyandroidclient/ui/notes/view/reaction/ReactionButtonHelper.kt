package jp.panta.misskeyandroidclient.ui.notes.view.reaction

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R

object ReactionButtonHelper {

    @JvmStatic
    @BindingAdapter("isReacted")
    fun ImageButton.setIsReacted(isReacted: Boolean?){
        if(isReacted == true){
            this.setImageResource(R.drawable.ic_remove_black_24dp)
        }else{
            this.setImageResource(R.drawable.ic_add_black_24dp)
        }
    }
}