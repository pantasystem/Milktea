package jp.panta.misskeyandroidclient.view.notes.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteEditorFilePreviewBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.FileNoteEditorData
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel

class SimpleImagePreviewAdapter(private val viewModel: NoteEditorViewModel) : ListAdapter<FileNoteEditorData,SimpleImagePreviewAdapter.SimpleImagePreviewHolder>(ItemCallback()){
    private class ItemCallback: DiffUtil.ItemCallback<FileNoteEditorData>(){
        override fun areContentsTheSame(
            oldItem: FileNoteEditorData,
            newItem: FileNoteEditorData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: FileNoteEditorData,
            newItem: FileNoteEditorData
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
        holder.binding.fileNoteEditorData = getItem(position)
        holder.binding.noteEditorViewModel = viewModel
        holder.binding.checkBox.isChecked = true
    }
}