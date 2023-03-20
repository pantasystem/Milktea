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
import com.google.android.flexbox.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemSimpleNoteBinding
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class NoteChildConversationAdapter(
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

    override fun onBindViewHolder(holder: SimpleNoteHolder, position: Int) {
        holder.binding.note = getItem(position)
        holder.binding.noteCardActionListener = actionAdapter
        setReactionCounter(getItem(position), holder.binding.reactionView)
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleNoteHolder {
        val binding = DataBindingUtil.inflate<ItemSimpleNoteBinding>(LayoutInflater.from(parent.context), R.layout.item_simple_note, parent, false)
        return SimpleNoteHolder(binding)
    }

    private var job: Job? = null

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView){

        val reactionList = note.reactionCountsViewData.value
        val adapter = ReactionCountAdapter {
            actionAdapter.onReactionCountAction(it)
        }
        adapter.note = note
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        job?.cancel()
        job = note.reactionCountsViewData.onEach {
            adapter.submitList(it.toList())
        }.flowWithLifecycle(lifecycleOwner.lifecycle).launchIn(lifecycleOwner.lifecycleScope)

        val exLayoutManager = reactionView.layoutManager
        if(exLayoutManager !is FlexboxLayoutManager){
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionView.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionView.layoutManager = flexBoxLayoutManager
        }

        if(reactionList.isNotEmpty()){
            reactionView.visibility = View.VISIBLE
        }

    }
}