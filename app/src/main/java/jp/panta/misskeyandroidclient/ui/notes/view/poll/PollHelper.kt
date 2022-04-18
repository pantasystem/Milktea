package jp.panta.misskeyandroidclient.ui.notes.view.poll

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel

object PollHelper {

    @BindingAdapter("noteId", "poll", "notesViewModel")
    @JvmStatic
    fun RecyclerView.bindPollChoices(noteId: net.pantasystem.milktea.model.notes.Note.Id?, poll: net.pantasystem.milktea.model.notes.poll.Poll?, notesViewModel: NotesViewModel?) {
        if (noteId == null || poll == null || notesViewModel == null) {
            this.visibility = View.GONE
            return
        } else {
            this.visibility = View.VISIBLE
        }

        val adapter = PollListAdapter(noteId = noteId, poll, notesViewModel)
        val layoutManager = this.layoutManager as? LinearLayoutManager
            ?: LinearLayoutManager(this.context)

        this.adapter = adapter
        this.layoutManager = layoutManager
        adapter.submitList(poll.choices)
    }
}