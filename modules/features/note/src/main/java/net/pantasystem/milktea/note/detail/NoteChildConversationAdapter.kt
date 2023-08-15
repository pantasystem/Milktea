package net.pantasystem.milktea.note.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemSimpleNoteBinding
import net.pantasystem.milktea.note.timeline.NoteFontSizeBinder
import net.pantasystem.milktea.note.timeline.ReactionCountItemsFlexboxLayoutBinder
import net.pantasystem.milktea.note.timeline.ViewRecycler
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class NoteChildConversationAdapter(
    val configRepository: LocalConfigRepository,
    val lifecycleOwner: LifecycleOwner,
    val onAction: (NoteCardAction) -> Unit,
) : ListAdapter<PlaneNoteViewData, NoteChildConversationAdapter.SimpleNoteHolder>(object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
    override fun areContentsTheSame(
        oldItem: PlaneNoteViewData,
        newItem: PlaneNoteViewData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areItemsTheSame(oldItem: PlaneNoteViewData, newItem: PlaneNoteViewData): Boolean {
        return oldItem.id == newItem.id
    }
}){

    class SimpleNoteHolder(val binding: ItemSimpleNoteBinding) : RecyclerView.ViewHolder(binding.root)

    private val actionAdapter = NoteCardActionListenerAdapter(onAction)

    private val reactionCountBinder = ReactionCountItemsFlexboxLayoutBinder(
        ViewRecycler()
    ) {
        actionAdapter.onReactionCountAction(it)
    }

    override fun onBindViewHolder(holder: SimpleNoteHolder, position: Int) {
        holder.binding.note = getItem(position)
        holder.binding.noteCardActionListener = actionAdapter

        setReactionCounter(getItem(position), holder.binding.reactionView)
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleNoteHolder {
        val binding = DataBindingUtil.inflate<ItemSimpleNoteBinding>(LayoutInflater.from(parent.context), R.layout.item_simple_note, parent, false)
        val config = configRepository.get().getOrNull() ?: DefaultConfig.config
        NoteFontSizeBinder.from(binding).bind(
            headerFontSize = config.noteHeaderFontSize,
            contentFontSize = config.noteContentFontSize,
        )
        return SimpleNoteHolder(binding)
    }

    private var job: Job? = null

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: FlexboxLayout){

        val reactionList = note.reactionCountsViewData.value

        job?.cancel()
        job = note.reactionCountsViewData.onEach {
            reactionCountBinder.bindReactionCounts(reactionView, note, it)
        }.flowWithLifecycle(lifecycleOwner.lifecycle).launchIn(lifecycleOwner.lifecycleScope)


        if(reactionList.isNotEmpty()){
            reactionView.visibility = View.VISIBLE
        } else {
            reactionView.visibility = View.GONE
        }

    }
}