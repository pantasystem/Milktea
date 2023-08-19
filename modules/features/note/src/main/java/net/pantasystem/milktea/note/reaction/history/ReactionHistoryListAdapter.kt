package net.pantasystem.milktea.note.reaction.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.note.reaction.LegacyReaction
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemReactionHistoryHeaderBinding
import net.pantasystem.milktea.note.databinding.ItemSimpleUserBinding

class ReactionHistoryListAdapter(
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<ReactionHistoryListType, ReactionHistoryViewHolder>(Diff()) {

    class Diff : DiffUtil.ItemCallback<ReactionHistoryListType>() {
        override fun areContentsTheSame(
            oldItem: ReactionHistoryListType,
            newItem: ReactionHistoryListType
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: ReactionHistoryListType,
            newItem: ReactionHistoryListType
        ): Boolean {
            if (oldItem is ReactionHistoryListType.ItemUser && newItem is ReactionHistoryListType.ItemUser) {
                return oldItem.user.id == newItem.user.id
            }
            return oldItem == newItem
        }
    }

    inner class ViewHolder(val binding: ItemSimpleUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user
            binding.lifecycleOwner = lifecycleOwner
            binding.executePendingBindings()
        }
    }



    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ReactionHistoryListType.Header -> {
                VHType.Header.ordinal
            }
            is ReactionHistoryListType.ItemUser -> {
                VHType.User.ordinal
            }
            ReactionHistoryListType.Loading -> {
                VHType.Loading.ordinal
            }
        }
    }

    override fun onBindViewHolder(holder: ReactionHistoryViewHolder, position: Int) {
        when(val item = getItem(position)) {
            is ReactionHistoryListType.Header -> {
                (holder as ReactionHistoryViewHolder.HeaderView).onBind(item.emojiType)
            }
            is ReactionHistoryListType.ItemUser -> {
                (holder as ReactionHistoryViewHolder.UserView).onBind(item.user, item.account)
            }
            ReactionHistoryListType.Loading -> {
                // 何もしない
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHistoryViewHolder {
        return when (VHType.values()[viewType]) {
            VHType.Loading -> ReactionHistoryViewHolder.LoadingView(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reaction_history_loading, parent, false)
            )
            VHType.User -> ReactionHistoryViewHolder.UserView(
                DataBindingUtil.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), R.layout.item_simple_user, parent, false
                ), lifecycleOwner
            )
            VHType.Header -> ReactionHistoryViewHolder.HeaderView(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_reaction_history_header,
                    parent,
                    false
                )
            )
        }
    }

}

sealed class ReactionHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class LoadingView(view: View) : ReactionHistoryViewHolder(view)
    class UserView(val binding: ItemSimpleUserBinding, val lifecycleOwner: LifecycleOwner) :
        ReactionHistoryViewHolder(binding.root) {
        fun onBind(user: User, account: Account?) {
            binding.user = user
            binding.account = account
            binding.lifecycleOwner = lifecycleOwner
            binding.executePendingBindings()
        }
    }

    class HeaderView(val binding: ItemReactionHistoryHeaderBinding) :
        ReactionHistoryViewHolder(binding.root) {
        fun onBind(emojiType: EmojiType) {
            when (emojiType) {
                is EmojiType.CustomEmoji -> {
                    binding.customEmojiView.isVisible = true
                    binding.emojiView.isVisible = false
                    GlideApp.with(binding.customEmojiView)
                        .load(emojiType.emoji.uri ?: emojiType.emoji.url)
                        .into(binding.customEmojiView)
                }
                is EmojiType.Legacy -> {
                    binding.customEmojiView.isVisible = false
                    binding.emojiView.isVisible = true
                    binding.emojiView.text = requireNotNull(LegacyReaction.reactionMap[emojiType.type])
                }
                is EmojiType.UtfEmoji -> {
                    binding.customEmojiView.isVisible = false
                    binding.emojiView.isVisible = true
                    binding.emojiView.text = emojiType.code
                }
            }
        }
    }
}

internal enum class VHType {
    Loading, User, Header
}