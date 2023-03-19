package net.pantasystem.milktea.note.reaction

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.note.R

object ReactionButtonHelper {

    @JvmStatic
    @BindingAdapter("isReacted", "isAcceptingOnlyFavoriteReaction")
    fun ImageButton.setIsReacted(isReacted: Boolean?, isAcceptingOnlyFavoriteReaction: Boolean?){
        if(isReacted == true){
            this.setImageResource(R.drawable.ic_remove_black_24dp)
        }else{
            if (isAcceptingOnlyFavoriteReaction == true) {
                this.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            } else {
                this.setImageResource(R.drawable.ic_add_black_24dp)
            }

        }
    }
}