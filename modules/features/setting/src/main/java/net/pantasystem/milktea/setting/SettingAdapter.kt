package net.pantasystem.milktea.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.setting.databinding.ItemMoveSettingActivityPanelBinding
import net.pantasystem.milktea.setting.databinding.ItemSettingGroupBinding
import net.pantasystem.milktea.setting.viewmodel.Group
import net.pantasystem.milktea.setting.viewmodel.MoveSettingActivityPanel
import net.pantasystem.milktea.setting.viewmodel.Shared
import net.pantasystem.milktea.setting.viewmodel.SharedItem


class SettingAdapter(
    val viewLifecycleOwner: LifecycleOwner
) : ListAdapter<Shared, SettingAdapter.SharedHolder>(ItemCallback()){

    class ItemCallback : DiffUtil.ItemCallback<Shared>(){
        override fun areContentsTheSame(oldItem: Shared, newItem: Shared): Boolean {
            return equal(oldItem, newItem)
        }

        override fun areItemsTheSame(oldItem: Shared, newItem: Shared): Boolean {
            return equal(oldItem, newItem)
        }

        private fun equal(newItem: Shared, oldItem: Shared): Boolean{
            if(oldItem.javaClass.name != newItem.javaClass.name){
                return false
            }
            return if(oldItem is SharedItem && newItem is SharedItem){
                oldItem.key == newItem.key && oldItem.titleStringRes == newItem.titleStringRes
            }else{
                false
            }
        }
    }

    abstract class SharedHolder(view: View) : RecyclerView.ViewHolder(view)
    class ItemMoveSettingActivityPanelHolder(val binding: ItemMoveSettingActivityPanelBinding) : SharedHolder(
        binding.root
    )
    class ItemSettingGroupHolder(val binding: ItemSettingGroupBinding) : SharedHolder(binding.root)

    companion object{
        const val GROUP = 0
        const val MOVE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is Group -> GROUP
            is MoveSettingActivityPanel<*> -> MOVE
            else -> throw IllegalArgumentException("not found")
        }
    }
    override fun onBindViewHolder(holder: SharedHolder, position: Int) {
        when(holder){
            is ItemSettingGroupHolder -> {
                holder.binding.item = getItem(position) as Group
                val a = SettingAdapter(viewLifecycleOwner)
                a.submitList((getItem(position) as Group).items)
                holder.binding.childItemsView.apply {
                    layoutManager = LinearLayoutManager(holder.binding.root.context)
                    adapter = a

                }
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
            is ItemMoveSettingActivityPanelHolder -> {
                holder.binding.item = getItem(position) as MoveSettingActivityPanel<*>
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedHolder {
        return when(viewType){

            GROUP -> {
                val binding = makeBinding<ItemSettingGroupBinding>(
                    parent,
                    R.layout.item_setting_group
                )
                ItemSettingGroupHolder(binding)
            }
            MOVE -> {
                val binding = makeBinding<ItemMoveSettingActivityPanelBinding>(
                    parent,
                    R.layout.item_move_setting_activity_panel
                )
                ItemMoveSettingActivityPanelHolder(binding)
            }

            else -> throw IllegalArgumentException("not found")

        }
    }

    private fun <T : ViewDataBinding> makeBinding(parent: ViewGroup, @LayoutRes res: Int): T{
        return DataBindingUtil.inflate(LayoutInflater.from(parent.context), res, parent, false)
    }
}