package net.pantasystem.milktea.user.reaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.timeline.NoteFontSizeBinder
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.databinding.ItemUserReactionBinding

class UserReactionsListAdapter(
    private val configRepository: LocalConfigRepository,
    private val lifecycleOwner: LifecycleOwner,
    private val noteCardActionHandler: NoteCardActionListenerAdapter,
) : ListAdapter<UserReactionBindingModel, UserReactionViewHolder>(
    object : DiffUtil.ItemCallback<UserReactionBindingModel>() {
        override fun areContentsTheSame(
            oldItem: UserReactionBindingModel,
            newItem: UserReactionBindingModel,
        ): Boolean {
            return oldItem.reaction == newItem.reaction
        }

        override fun areItemsTheSame(
            oldItem: UserReactionBindingModel,
            newItem: UserReactionBindingModel,
        ): Boolean {
            return oldItem.reaction.id == newItem.reaction.id
        }
    }
) {
    override fun onBindViewHolder(holder: UserReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReactionViewHolder {
        val config = configRepository.get().getOrElse {
            DefaultConfig.config
        }
        val binding = DataBindingUtil.inflate<ItemUserReactionBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_user_reaction,
            parent,
            false
        )
        NoteFontSizeBinder.from(binding.simpleNote).bind(
            headerFontSize = config.noteHeaderFontSize,
            contentFontSize = config.noteContentFontSize
        )
        return UserReactionViewHolder(lifecycleOwner, binding, noteCardActionHandler)
    }
}

class UserReactionViewHolder(
    val lifecycleOwner: LifecycleOwner,
    val binding: ItemUserReactionBinding,
    val noteCardActionListenerAdapter: NoteCardActionListenerAdapter,
) : RecyclerView.ViewHolder(binding.root) {

    private var job: Job? = null
    fun bind(item: UserReactionBindingModel) {
        val listView = binding.simpleNote.reactionView
        listView.layoutManager = FlexboxLayoutManager(binding.root.context)
        val adapter = ReactionCountAdapter {
            noteCardActionListenerAdapter.onReactionCountAction(it)
        }
        adapter.note = item.note
        listView.adapter = adapter
        binding.noteCardActionListener = noteCardActionListenerAdapter
        binding.bindingModel = item

        job?.cancel()
        job = item.note.reactionCountsViewData.onEach {
            adapter.submitList(it)
        }.flowWithLifecycle(lifecycleOwner.lifecycle).launchIn(lifecycleOwner.lifecycleScope)

        binding.lifecycleOwner = lifecycleOwner
        binding.simpleNote.lifecycleOwner = lifecycleOwner
        binding.executePendingBindings()
    }
}