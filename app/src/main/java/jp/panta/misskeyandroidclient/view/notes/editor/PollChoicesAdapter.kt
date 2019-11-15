package jp.panta.misskeyandroidclient.view.notes.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemEditPollChoiceBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollChoice
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollEditor

class PollChoicesAdapter(private val pollEditor: PollEditor, private val lifecycleOwner: LifecycleOwner) : ListAdapter<PollChoice, PollChoicesAdapter.ChoiceHolder>(ItemCallback()){
    class ItemCallback() : DiffUtil.ItemCallback<PollChoice>(){
        override fun areContentsTheSame(oldItem: PollChoice, newItem: PollChoice): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: PollChoice, newItem: PollChoice): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class ChoiceHolder(val binding: ItemEditPollChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
        holder.binding.choice = getItem(position)
        holder.binding.pollViewModel = pollEditor
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder {
        val binding = DataBindingUtil.inflate<ItemEditPollChoiceBinding>(LayoutInflater.from(parent.context), R.layout.item_edit_poll_choice, parent, false)
        return ChoiceHolder(binding)
    }

}