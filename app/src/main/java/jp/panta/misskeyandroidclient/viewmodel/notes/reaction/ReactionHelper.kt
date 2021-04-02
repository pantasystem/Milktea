package jp.panta.misskeyandroidclient.viewmodel.notes.reaction

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

object ReactionHelper {

    @SuppressLint("UseCompatLoadingForDrawables")
    @JvmStatic
    @BindingAdapter("reactionNote", "reactionBackground")
    fun LinearLayout.setBackground(note: PlaneNoteViewData, reaction: ReactionCount){

        val r = reaction.reaction.replace(":", "")
        val host = r.split("@").getOrNull(1)
        if(host != null && host != ".") {
            this.background = ColorDrawable(Color.argb(0,0,0,0))
            return
        }
        if(note.myReaction.value != null && note.myReaction.value == reaction.reaction){
            this.background = context.resources.getDrawable(R.drawable.shape_selected_reaction_background, context.theme)
        }else{
            this.background = context.resources.getDrawable(R.drawable.shape_normal_reaction_backgruond, context.theme)
        }
    }
}