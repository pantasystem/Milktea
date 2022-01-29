package jp.panta.misskeyandroidclient.ui.notes.view.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.databinding.ItemMediaPreviewBinding
import jp.panta.misskeyandroidclient.ui.media.MediaPreviewHelper
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData

class PreviewAbleFileListAdapter(
    val media: MediaPreviewHelper
) : ListAdapter<FileViewData, PreviewAbleFileListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<FileViewData>() {
        override fun areContentsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem.file == newItem.file
        }

        override fun areItemsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem.file.localFileId == newItem.file.localFileId
                    && oldItem.file.remoteFileId == oldItem.file.remoteFileId
        }
    }
) {

    class ViewHolder(val binding: ItemMediaPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fileViewData: FileViewData) {
            binding.fileViewData = fileViewData
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(
            binding,
        )
    }
}