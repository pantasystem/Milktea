package net.pantasystem.milktea.note.reaction

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.note.R

object ReactionHelper {

    @SuppressLint("UseCompatLoadingForDrawables")
    @JvmStatic
    @BindingAdapter("myReaction", "reactionBackground")
    fun LinearLayout.setBackground(myReaction: String?, reaction: ReactionCount){

        if(!Reaction(reaction.reaction).isLocal()) {
            this.background = ColorDrawable(Color.argb(0,0,0,0))
            return
        }
        if(myReaction != null && myReaction == reaction.reaction){
            this.background = context.resources.getDrawable(R.drawable.shape_selected_reaction_background, context.theme)
        }else{
            this.background = context.resources.getDrawable(R.drawable.shape_normal_reaction_backgruond, context.theme)
        }
    }
}