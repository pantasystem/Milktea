package net.pantasystem.milktea.user.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.databinding.ItemUserProfileFieldBinding

class UserProfileFieldListAdapter : ListAdapter<User.Field, UserProfileFieldListAdapter.VH>(
    object : DiffUtil.ItemCallback<User.Field>() {
        override fun areContentsTheSame(oldItem: User.Field, newItem: User.Field): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: User.Field, newItem: User.Field): Boolean {
            return oldItem == newItem
        }
    }
) {
    class VH(val binding: ItemUserProfileFieldBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(field: User.Field) {
            binding.field = field
            binding.copyValueButton.setOnClickListener {
                val clipboardManager = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText(field.name, field.value))
            }
        }

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemUserProfileFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

}