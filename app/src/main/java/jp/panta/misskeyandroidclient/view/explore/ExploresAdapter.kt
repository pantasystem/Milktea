package jp.panta.misskeyandroidclient.view.explore

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SortedUsersActivity
import jp.panta.misskeyandroidclient.databinding.ItemExploreBinding
import jp.panta.misskeyandroidclient.viewmodel.explore.Explore

class ExploresAdapter : ListAdapter<Explore, ExploresAdapter.VH>(ItemCB){
    class VH(val binding: ItemExploreBinding) : RecyclerView.ViewHolder(binding.root)
    object ItemCB : DiffUtil.ItemCallback<Explore>(){
        override fun areContentsTheSame(oldItem: Explore, newItem: Explore): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Explore, newItem: Explore): Boolean {
            return oldItem.name == newItem.name
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val explore = getItem(position)
        holder.binding.explore = explore
        holder.binding.exploreItem.setOnClickListener{
            when(explore){
                is Explore.User ->{
                    val intent = Intent(it.context, SortedUsersActivity::class.java)
                    intent.putExtra(SortedUsersActivity.EXTRA_ORIGIN, explore.origin)
                    intent.putExtra(SortedUsersActivity.EXTRA_SORT, explore.sort)
                    intent.putExtra(SortedUsersActivity.EXTRA_STATE, explore.state)
                    intent.putExtra(SortedUsersActivity.EXTRA_TITLE, explore.name)
                    it.context.startActivity(intent)
                }
                is Explore.Tag ->{

                }
                is Explore.UserType ->{
                    val intent = Intent(it.context, SortedUsersActivity::class.java)
                    intent.putExtra(SortedUsersActivity.EXTRA_SORTED_USERS_TYPE, explore.type)
                    intent.putExtra(SortedUsersActivity.EXTRA_TITLE, explore.name)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_explore,
                parent,
                false
            )
        )
    }
}