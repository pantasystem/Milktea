package net.pantasystem.milktea.note.poll

import android.view.View
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter

object PollHelper {

    @BindingAdapter("noteId", "poll", "noteCardActionListenerAdapter")
    @JvmStatic
    fun LinearLayout.bindPollChoices(noteId: Note.Id?, poll: Poll?, noteCardActionListenerAdapter: NoteCardActionListenerAdapter?) {
        if (noteId == null || poll == null || noteCardActionListenerAdapter == null) {
            this.visibility = View.GONE
            return
        } else {
            this.visibility = View.VISIBLE
        }
        PollListLinearLayoutBinder.bindPollChoices(this, noteId, poll, noteCardActionListenerAdapter)
//        PollListLinearLayoutBinder.bindVoteResult(this, poll.totalVoteCount)
    }
}