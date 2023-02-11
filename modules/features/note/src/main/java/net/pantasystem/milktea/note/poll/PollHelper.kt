package net.pantasystem.milktea.note.poll

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter

object PollHelper {

    @BindingAdapter("noteId", "poll", "noteCardActionListenerAdapter")
    @JvmStatic
    fun RecyclerView.bindPollChoices(noteId: Note.Id?, poll: Poll?, noteCardActionListenerAdapter: NoteCardActionListenerAdapter?) {
        if (noteId == null || poll == null || noteCardActionListenerAdapter == null) {
            this.visibility = View.GONE
            return
        } else {
            this.visibility = View.VISIBLE
        }

        val adapter = PollListAdapter(noteId = noteId, poll) {
            noteCardActionListenerAdapter.onPollChoiceClicked(it.noteId, poll, it.choice)
        }
        val layoutManager = this.layoutManager as? LinearLayoutManager
            ?: LinearLayoutManager(this.context)
        layoutManager.recycleChildrenOnDetach = true
        this.adapter = adapter
        this.layoutManager = layoutManager
        adapter.submitList(poll.choices)
    }
}