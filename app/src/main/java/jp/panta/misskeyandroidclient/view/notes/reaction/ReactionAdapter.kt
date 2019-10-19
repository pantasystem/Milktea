package jp.panta.misskeyandroidclient.view.notes.reaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionBinding

class ReactionAdapter(diffUtilCallBack: DiffUtil.ItemCallback<Pair<String, Int>>) : ListAdapter<Pair<String, Int>, ReactionAdapter.ReactionHolder>(diffUtilCallBack){
    class ReactionHolder(val binding: ItemReactionBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ReactionHolder, position: Int) {
        val item =  getItem(position)
        holder.binding.reaction = item//Pair(java.lang.String(item.first), Integer.valueOf(item.second))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHolder {
        val binding = DataBindingUtil.inflate<ItemReactionBinding>(LayoutInflater.from(parent.context), R.layout.item_reaction, parent, false)
        return ReactionHolder(binding)
    }
}