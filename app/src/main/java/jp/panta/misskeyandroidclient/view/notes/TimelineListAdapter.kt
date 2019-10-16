package jp.panta.misskeyandroidclient.view.notes

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.util.ObservableArrayListAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class TimelineListAdapter(private val observableList: ObservableArrayList<PlaneNoteViewData>) : ObservableArrayListAdapter<PlaneNoteViewData, TimelineListAdapter.NoteViewHolder>(observableList){

    class NoteViewHolder(val binding: ItemNoteBinding): RecyclerView.ViewHolder(binding.root)
    override fun getItemCount(): Int {
        return observableList.size
    }

    override fun onBindViewHolder(p0: NoteViewHolder, p1: Int) {
        //p0.binding.note = observableList[p1]
        p0.binding.note = observableList[p1]
        p0.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteViewHolder {
        val binding = DataBindingUtil.inflate<ItemNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_note, p0, false)
        return NoteViewHolder(binding)
    }


}

