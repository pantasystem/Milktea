package jp.panta.misskeyandroidclient.view.notes.reaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionPreviewBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel

class ReactionPreviewAdapter(diffUtilCallBack: DiffUtil.ItemCallback<String>) : ListAdapter<String, ReactionPreviewAdapter.ReactionPreviewViewHolder>(diffUtilCallBack){
    class ReactionPreviewViewHolder(val binding: ItemReactionPreviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ReactionPreviewViewHolder, position: Int) {
        holder.binding.reaction = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionPreviewViewHolder {
        val binding = DataBindingUtil.inflate<ItemReactionPreviewBinding>(LayoutInflater.from(parent.context), R.layout.item_reaction_preview, parent, false)

        return ReactionPreviewViewHolder(binding)
    }
}