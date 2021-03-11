package jp.panta.misskeyandroidclient.view.notes

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.Activities
import jp.panta.misskeyandroidclient.NoteDetailActivity
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.putActivity

object NoteTransitionHelper {

    @JvmStatic
    @BindingAdapter("clickedView", "transitionDestinationNote")
    fun View.transitionNoteDetail(clickedView: View?, transitionDestinationNote: Note){
        transitionDestinationNote?: return
        val clicked = clickedView?: this
        clicked.setOnClickListener {
            val context = this.context
            val intent = Intent(context, NoteDetailActivity::class.java)
            intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, transitionDestinationNote.id.noteId)
            intent.putActivity(Activities.ACTIVITY_IN_APP)

            if(context is Activity){
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(context, this, "note")
                context.startActivity(intent, compat.toBundle())
            }else{
                context.startActivity(intent)
            }
        }

    }
}