package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteEditorFilePreviewBinding
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener

class SimpleImagePreviewAdapter(private val fileListener: FileListener) : ListAdapter<File, SimpleImagePreviewAdapter.SimpleImagePreviewHolder>(
    ItemCallback()
){
    private class ItemCallback: DiffUtil.ItemCallback<File>(){
        override fun areContentsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem == newItem
        }
    }
    class SimpleImagePreviewHolder(val binding: ItemNoteEditorFilePreviewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleImagePreviewHolder {
        val binding = DataBindingUtil.inflate<ItemNoteEditorFilePreviewBinding>(LayoutInflater.from(parent.context), R.layout.item_note_editor_file_preview, parent, false)
        return SimpleImagePreviewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleImagePreviewHolder, position: Int) {
        holder.binding.file = getItem(position)
        holder.binding.fileListener = fileListener
        holder.binding.checkBox.isChecked = true
    }
}