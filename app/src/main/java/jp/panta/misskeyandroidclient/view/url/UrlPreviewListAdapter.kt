package jp.panta.misskeyandroidclient.view.url

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemUrlPreviewBinding
import jp.panta.misskeyandroidclient.model.url.UrlPreview

class UrlPreviewListAdapter : ListAdapter<UrlPreview, UrlPreviewListAdapter.ViewHolder>(ItemCallback()){
    class ItemCallback : DiffUtil.ItemCallback<UrlPreview>(){
        override fun areContentsTheSame(oldItem: UrlPreview, newItem: UrlPreview): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: UrlPreview, newItem: UrlPreview): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(val binding: ItemUrlPreviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.urlPreview = getItem(position)
        holder.binding.urlPreviewView.setOnClickListener {
            val context = holder.binding.urlPreviewView.context
            context?.startActivity(
                Intent(Intent.ACTION_VIEW).apply{
                    data = Uri.parse(getItem(position).url)
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_url_preview,
                parent,
                 false
            )
        )
    }
}