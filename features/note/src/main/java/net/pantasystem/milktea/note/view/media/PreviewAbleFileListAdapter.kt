package net.pantasystem.milktea.note.view.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.note.databinding.ItemMediaPreviewBinding
import net.pantasystem.milktea.note.viewmodel.media.MediaViewData
import net.pantasystem.milktea.note.viewmodel.media.PreviewAbleFile

class PreviewAbleFileListAdapter(
    val media: MediaViewData
) : ListAdapter<PreviewAbleFile, PreviewAbleFileListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<PreviewAbleFile>() {
        override fun areContentsTheSame(oldItem: PreviewAbleFile, newItem: PreviewAbleFile): Boolean {
            return oldItem.file == newItem.file
        }

        override fun areItemsTheSame(oldItem: PreviewAbleFile, newItem: PreviewAbleFile): Boolean {
            return oldItem.file.localFileId == newItem.file.localFileId
                    && oldItem.file.remoteFileId == oldItem.file.remoteFileId
        }
    }
) {

    class ViewHolder(val binding: ItemMediaPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fileViewData: PreviewAbleFile, media: MediaViewData) {
            binding.previewAbleFile = fileViewData
            binding.mediaViewData = media
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), media)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(
            binding,
        )
    }
}