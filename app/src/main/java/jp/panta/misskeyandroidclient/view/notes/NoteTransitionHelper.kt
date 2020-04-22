package jp.panta.misskeyandroidclient.view.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.NoteDetailActivity
import jp.panta.misskeyandroidclient.model.notes.Note

object NoteTransitionHelper {

    @JvmStatic
    @BindingAdapter("clickedView", "transitionDestinationNote")
    fun View.transitionNoteDetail(clickedView: View?, transitionDestinationNote: Note?){
        transitionDestinationNote?: return
        clickedView?: return
        clickedView.setOnClickListener {
            val context = this.context
            val intent = Intent(context, NoteDetailActivity::class.java)
            intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, transitionDestinationNote.id)
            if(context is Activity){
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(context, this, "note")
                context.startActivity(intent, compat.toBundle())
            }else{
                context.startActivity(intent)
            }
        }

    }
}