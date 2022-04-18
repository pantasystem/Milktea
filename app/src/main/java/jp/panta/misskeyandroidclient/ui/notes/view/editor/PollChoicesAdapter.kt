package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemEditPollChoiceBinding
import net.pantasystem.milktea.model.notes.PollChoiceState
import java.util.*

class PollChoicesAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val onChoiceTextChangedListener: (UUID, String)-> Unit,
    private val onChoiceDeleteButtonClickListener: (UUID) -> Unit
) : ListAdapter<net.pantasystem.milktea.model.notes.PollChoiceState, PollChoicesAdapter.ChoiceHolder>(ItemCallback()){
    class ItemCallback : DiffUtil.ItemCallback<net.pantasystem.milktea.model.notes.PollChoiceState>(){
        override fun areContentsTheSame(oldItem: net.pantasystem.milktea.model.notes.PollChoiceState, newItem: net.pantasystem.milktea.model.notes.PollChoiceState): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: net.pantasystem.milktea.model.notes.PollChoiceState, newItem: net.pantasystem.milktea.model.notes.PollChoiceState): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class ChoiceHolder(
        val binding: ItemEditPollChoiceBinding,
        private val onChoiceTextChangedListener: (UUID, String) -> Unit,
        private val onChoiceDeleteButtonClickListener: (UUID) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var choice: net.pantasystem.milktea.model.notes.PollChoiceState? = null

        val textChangedListener: (Editable?)->Unit = {
            choice?.let{ choice ->
                onChoiceTextChangedListener.invoke(choice.id, it?.toString()?: "")
            }
        }
        fun bind(choice: net.pantasystem.milktea.model.notes.PollChoiceState) {
            this.choice = choice
            binding.deleteButton.setOnClickListener {
                onChoiceDeleteButtonClickListener.invoke(choice.id)
            }
            binding.choice = choice

        }
    }

    override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.lifecycleOwner = lifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder {
        val binding = DataBindingUtil.inflate<ItemEditPollChoiceBinding>(LayoutInflater.from(parent.context), R.layout.item_edit_poll_choice, parent, false)
        val holder = ChoiceHolder(binding, onChoiceTextChangedListener, onChoiceDeleteButtonClickListener)
        binding.inputChoiceText.addTextChangedListener {
            holder.textChangedListener.invoke(it)
        }
        return holder
    }

}