package net.pantasystem.milktea.note.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.pantasystem.milktea.note.databinding.ItemMediaPreviewBinding
import net.pantasystem.milktea.note.media.viewmodel.MediaViewData
import net.pantasystem.milktea.note.media.viewmodel.PreviewAbleFile

class PreviewAbleFileListAdapter(
    val media: MediaViewData
) : ListAdapter<PreviewAbleFile, PreviewAbleFileListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<PreviewAbleFile>() {
        override fun areContentsTheSame(oldItem: PreviewAbleFile, newItem: PreviewAbleFile): Boolean {
            return oldItem.source == newItem.source
        }

        override fun areItemsTheSame(oldItem: PreviewAbleFile, newItem: PreviewAbleFile): Boolean {
            return oldItem == newItem
        }
    }
) {

    class ViewHolder(val binding: ItemMediaPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(index: Int, fileViewData: PreviewAbleFile, media: MediaViewData) {
            binding.previewAbleFileIndex = index
            binding.previewAbleFile = fileViewData
            binding.mediaViewData = media
            binding.executePendingBindings()

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, getItem(position), media)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(
            binding,
        )
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        Glide.with(holder.binding.thumbnail).clear(holder.binding.thumbnail)
    }
}