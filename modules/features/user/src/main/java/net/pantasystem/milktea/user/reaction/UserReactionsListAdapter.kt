package net.pantasystem.milktea.user.reaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.databinding.ItemUserReactionBinding

class UserReactionsListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val noteCardActionHandler: NoteCardActionListenerAdapter
) : ListAdapter<UserReactionBindingModel, UserReactionViewHolder>(
    object : DiffUtil.ItemCallback<UserReactionBindingModel>() {
        override fun areContentsTheSame(
            oldItem: UserReactionBindingModel,
            newItem: UserReactionBindingModel
        ): Boolean {
            return oldItem.reaction == newItem.reaction
        }

        override fun areItemsTheSame(
            oldItem: UserReactionBindingModel,
            newItem: UserReactionBindingModel
        ): Boolean {
            return oldItem.reaction.id == newItem.reaction.id
        }
    }
) {
    override fun onBindViewHolder(holder: UserReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReactionViewHolder {
        val binding = DataBindingUtil.inflate<ItemUserReactionBinding>(LayoutInflater.from(parent.context), R.layout.item_user_reaction, parent, false)
        return UserReactionViewHolder(lifecycleOwner, binding, noteCardActionHandler)
    }
}

class UserReactionViewHolder(
    val lifecycleOwner: LifecycleOwner,
    val binding: ItemUserReactionBinding,
    val noteCardActionListenerAdapter: NoteCardActionListenerAdapter,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UserReactionBindingModel) {
        val listView = binding.simpleNote.reactionView
        listView.layoutManager = FlexboxLayoutManager(binding.root.context)
        val adapter = ReactionCountAdapter(lifecycleOwner) {
            noteCardActionListenerAdapter.onReactionCountAction(it)
        }
        adapter.note = item.note
        listView.adapter = adapter
        binding.noteCardActionListener = noteCardActionListenerAdapter
        binding.bindingModel = item

        item.note.reactionCountsViewData.observe(lifecycleOwner) {
            adapter.submitList(it)
        }
        binding.lifecycleOwner = lifecycleOwner
        binding.simpleNote.lifecycleOwner = lifecycleOwner
        binding.executePendingBindings()
    }
}