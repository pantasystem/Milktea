package jp.panta.misskeyandroidclient.view.notes

import android.databinding.DataBindingUtil
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData


class TimelineListAdapter(diffUtil: DiffUtil.ItemCallback<PlaneNoteViewData>) : ListAdapter<PlaneNoteViewData, TimelineListAdapter.NoteViewHolder>(diffUtil) {

    class NoteViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteViewHolder {
        val binding = DataBindingUtil.inflate<ItemNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_note, p0, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(p0: NoteViewHolder, p1: Int) {
        //Log.d("", "index: $p1")
        p0.binding.note = getItem(p1)
    }
}