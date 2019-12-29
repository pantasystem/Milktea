package jp.panta.misskeyandroidclient.view.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemAppBinding
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.viewmodel.auth.custom.CustomAppViewModel

class AppListAdapter(
    val viewLifecycleOwner: LifecycleOwner,
    val customAppViewModel: CustomAppViewModel
) : ListAdapter<App, AppListAdapter.ViewHolder>(DiffUtilItemCallback()){
    class DiffUtilItemCallback : DiffUtil.ItemCallback<App>(){
        override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemAppBinding>(LayoutInflater.from(parent.context), R.layout.item_app, parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.lifecycleOwner = viewLifecycleOwner
        holder.binding.app = getItem(position)
        holder.binding.customAppViewModel = customAppViewModel
        holder.binding.executePendingBindings()
    }
}