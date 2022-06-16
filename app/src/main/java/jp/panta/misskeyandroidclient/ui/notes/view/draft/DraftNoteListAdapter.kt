package jp.panta.misskeyandroidclient.ui.notes.view.draft

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemDraftNoteBinding
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft.DraftNoteUiState

class DraftNoteListAdapter(
    private val draftNoteActionCallback: DraftNoteActionCallback,
    val fileListener: FileListener,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<DraftNoteUiState, DraftNoteListAdapter.VH>(DFUtilItemCallback()){

    class VH(val binding: ItemDraftNoteBinding) : RecyclerView.ViewHolder(binding.root)

    class DFUtilItemCallback : DiffUtil.ItemCallback<DraftNoteUiState>(){
        override fun areContentsTheSame(oldItem: DraftNoteUiState, newItem: DraftNoteUiState): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: DraftNoteUiState, newItem: DraftNoteUiState): Boolean {
            return oldItem.draftNote.draftNoteId == newItem.draftNote.draftNoteId
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.note = getItem(position)
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.draftNoteAction = draftNoteActionCallback
        holder.binding.fileListener = fileListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_draft_note,
                parent,
                false
            )
        )
    }
}