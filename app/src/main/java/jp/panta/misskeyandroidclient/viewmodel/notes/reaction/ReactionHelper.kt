package jp.panta.misskeyandroidclient.viewmodel.notes.reaction

import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

object ReactionHelper {

    @JvmStatic
    @BindingAdapter("reactionNote", "reactionBackground")
    fun LinearLayout.setBackground(note: PlaneNoteViewData, reaction: Pair<String, Int>){
        if(note.myReaction.value != null && note.myReaction.value == reaction.first){
            this.background = context.resources.getDrawable(R.drawable.shape_selected_reaction_background, context.theme)
        }else{
            this.background = context.resources.getDrawable(R.drawable.shape_normal_reaction_backgruond, context.theme)
        }
    }
}