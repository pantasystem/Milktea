package jp.panta.misskeyandroidclient.view.notes


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.util.ObservableArrayListAdapter
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel

class TimelineListAdapter(
    diffUtilCallBack: DiffUtil.ItemCallback<PlaneNoteViewData>,
    private val lifecycleOwner: LifecycleOwner,
    private val notesViewModel: NotesViewModel
) : ListAdapter<PlaneNoteViewData, TimelineListAdapter.NoteViewHolder>(diffUtilCallBack){

    class NoteViewHolder(val binding: ItemNoteBinding): RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(p0: NoteViewHolder, p1: Int) {
        //p0.binding.note = observableList[p1]
        p0.binding.note = getItem(p1)
        val adapter =ReactionAdapter(
            object : DiffUtil.ItemCallback<Pair<String, Int>>(){
                override fun areContentsTheSame(
                    oldItem: Pair<String, Int>,
                    newItem: Pair<String, Int>
                ): Boolean {
                    return oldItem == newItem
                }

                override fun areItemsTheSame(
                    oldItem: Pair<String, Int>,
                    newItem: Pair<String, Int>
                ): Boolean {
                    return oldItem.first == newItem.first
                }
            }
        , getItem(p1), notesViewModel)
        adapter.submitList(getItem(p1).reactionCounts.value?.toList())
        getItem(p1).reactionCounts.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toList())
        })
        p0.binding.reactionView.adapter = adapter
        p0.binding.reactionView.layoutManager = LinearLayoutManager(p0.binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        p0.binding.lifecycleOwner = lifecycleOwner
        p0.binding.executePendingBindings()
        p0.binding.notesViewModel = notesViewModel
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteViewHolder {
        val binding = DataBindingUtil.inflate<ItemNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_note, p0, false)
        return NoteViewHolder(binding)
    }


}

