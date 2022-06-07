package jp.panta.misskeyandroidclient.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemFollowingFollowerBinding
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ShowUserDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.model.user.User

fun interface OnToggleFollowListener {
    fun toggle(userId: User.Id)
}

@FlowPreview
@ExperimentalCoroutinesApi
class FollowableUserListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val showUserDetails: ShowUserDetails,
    private val onToggleFollowListener: OnToggleFollowListener,
) : ListAdapter<User.Detail, FollowableUserListAdapter.ViewHolder>(
    DiffUtilItemCallback()
){
    @FlowPreview
    @ExperimentalCoroutinesApi
    class DiffUtilItemCallback : DiffUtil.ItemCallback<User.Detail>(){
        override fun areContentsTheSame(oldItem: User.Detail, newItem: User.Detail): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: User.Detail, newItem: User.Detail): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class ViewHolder(val binding: ItemFollowingFollowerBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.user = getItem(position)
        holder.binding.lifecycleOwner = viewLifecycleOwner
        holder.binding.showUserDetails = showUserDetails
        holder.binding.toggleFollowListener = onToggleFollowListener
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemFollowingFollowerBinding>(LayoutInflater.from(parent.context), R.layout.item_following_follower, parent, false)
        return ViewHolder(binding)
    }
}