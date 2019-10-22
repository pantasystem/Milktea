package jp.panta.misskeyandroidclient.view.notes.reaction

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class ReactionAdapter(diffUtilCallBack: DiffUtil.ItemCallback<Pair<String, Int>>, private val note: PlaneNoteViewData, private val lifecycleOwner: LifecycleOwner) : ListAdapter<Pair<String, Int>, ReactionAdapter.ReactionHolder>(diffUtilCallBack){
    class ReactionHolder(val binding: ItemReactionBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ReactionHolder, position: Int) {
        val item =  getItem(position)
        holder.binding.reaction = item//Pair(java.lang.String(item.first), Integer.valueOf(item.second))
        holder.binding.note = note
        //holder.binding.lifecycleOwner = lifecycleOwner
        //Log.d("ReactionAdapter", "reaction: ${item.first} ,")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHolder {
        val binding = DataBindingUtil.inflate<ItemReactionBinding>(LayoutInflater.from(parent.context), R.layout.item_reaction, parent, false)
        return ReactionHolder(binding)
    }
}