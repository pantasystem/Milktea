package jp.panta.misskeyandroidclient.ui.notes.view.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemSimpleNoteBinding
import net.pantasystem.milktea.data.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

class NoteChildConversationAdapter(
    val notesViewModel: NotesViewModel,
    val lifecycleOwner: LifecycleOwner
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

    override fun onBindViewHolder(holder: SimpleNoteHolder, position: Int) {
        holder.binding.note = getItem(position)
        setReactionCounter(getItem(position), holder.binding.reactionView)
        holder.binding.notesViewModel = notesViewModel
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleNoteHolder {
        val binding = DataBindingUtil.inflate<ItemSimpleNoteBinding>(LayoutInflater.from(parent.context), R.layout.item_simple_note, parent, false)
        return SimpleNoteHolder(binding)
    }

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView){

        val reactionList = note.reactionCounts.value?.toList()?: emptyList()
        val adapter = ReactionCountAdapter(notesViewModel)
        adapter.note = note
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        val observer = Observer<List<ReactionCount>> {
            adapter.submitList(it.toList())
        }
        note.reactionCounts.observe(lifecycleOwner, observer)

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