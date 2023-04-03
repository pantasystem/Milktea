package net.pantasystem.milktea.note.reaction

import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.note.R

object ReactionHelper {

    fun LinearLayout.applyBackgroundColor(reaction: ReactionViewData, isMisskey: Boolean){

        // NOTE: Misskeyはローカルに存在するカスタム絵文字しかリアクションすることができない
        if (isMisskey) {
            if(!Reaction(reaction.reaction).isLocal()) {
                this.background = ContextCompat.getDrawable(context, R.drawable.shape_normal_reaction_backgruond)?.apply {
                    alpha = 75
                }
                return
            }
        }

        if(reaction.isMyReaction){
            this.background = ContextCompat.getDrawable(context, R.drawable.shape_selected_reaction_background)
        }else{
            this.background = ContextCompat.getDrawable(context, R.drawable.shape_normal_reaction_backgruond)
        }
    }
}