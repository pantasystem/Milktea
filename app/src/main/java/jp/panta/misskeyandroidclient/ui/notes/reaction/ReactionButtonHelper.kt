package jp.panta.misskeyandroidclient.ui.notes.reaction

import android.view.View
import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R

object ReactionButtonHelper {

    @JvmStatic
    @BindingAdapter("isReacted")
    fun ImageButton.setIsReacted(isReacted: Boolean?){
        if(isReacted == true){
            this.setImageResource(R.drawable.ic_remove_circle_outline_black_24dp)
        }else{
            this.setImageResource(R.drawable.ic_add_circle_outline_black_24dp)
        }
    }
}