package jp.panta.misskeyandroidclient.ui.tags

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SearchResultActivity
import jp.panta.misskeyandroidclient.databinding.ItemTagBinding
import net.pantasystem.milktea.data.model.hashtag.HashTag

class HashTagListAdapter : ListAdapter<HashTag, HashTagListAdapter.VH>(ItemCallback()){

    class VH(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root)

    class ItemCallback : DiffUtil.ItemCallback<HashTag>(){
        override fun areContentsTheSame(oldItem: HashTag, newItem: HashTag): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: HashTag, newItem: HashTag): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tag = getItem(position)
        holder.binding.hashTag = tag.tag
        holder.binding.tagChip.setOnClickListener {
            val intent = Intent(it.context, SearchResultActivity::class.java)
            intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, "#${tag.tag}")
            it.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_tag,
                parent,
                false
            )
        )
    }
}