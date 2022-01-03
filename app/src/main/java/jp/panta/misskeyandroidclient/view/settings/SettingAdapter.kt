package jp.panta.misskeyandroidclient.view.settings

import android.text.InputType
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
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.*
import jp.panta.misskeyandroidclient.viewmodel.setting.*


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
    class ItemSharedCheckboxHolder(val binding: ItemSharedCheckboxBinding) : SharedHolder(binding.root)
    class ItemSharedSwitchHolder(val binding: ItemSharedSwitchBinding) : SharedHolder(binding.root)
    class ItemMoveSettingActivityPanelHolder(val binding: ItemMoveSettingActivityPanelBinding) : SharedHolder(
        binding.root
    )
    class ItemSettingGroupHolder(val binding: ItemSettingGroupBinding) : SharedHolder(binding.root)
    class ItemSettingSelectionHolder(val binding: ItemSharedSelectionBinding) : SharedHolder(binding.root)
    class ItemSharedTextHolder(val binding: ItemSharedTextBinding) : SharedHolder(binding.root)

    companion object{
        const val GROUP = 0
        const val CHECK = 1
        const val SWITCH = 2
        const val MOVE = 3
        const val SELECTION = 4
        const val TEXT = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when(val item = getItem(position)){
            is Group -> GROUP
            is BooleanSharedItem -> {
                when (item.choiceType) {
                    BooleanSharedItem.ChoiceType.CHECK_BOX -> CHECK
                    BooleanSharedItem.ChoiceType.SWITCH -> SWITCH
                }
            }
            is MoveSettingActivityPanel<*> -> MOVE
            is SelectionSharedItem -> SELECTION
            is TextSharedItem -> TEXT
            else -> throw IllegalArgumentException("not found")
        }
    }
    override fun onBindViewHolder(holder: SharedHolder, position: Int) {
        when(holder){
            is ItemSharedSwitchHolder -> {
                holder.binding.item = getItem(position) as BooleanSharedItem
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
            is ItemSharedCheckboxHolder -> {
                holder.binding.item = getItem(position) as BooleanSharedItem
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
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
            is ItemSettingSelectionHolder -> {
                val item = getItem(position) as SelectionSharedItem
                holder.binding.selection = item
                holder.binding.selectionList.layoutManager =
                    LinearLayoutManager(holder.itemView.context)
                val adapter = SelectionListAdapter(viewLifecycleOwner, item)
                holder.binding.selectionList.adapter = adapter
                adapter.submitList(item.choices)
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()

            }
            is ItemSharedTextHolder -> {
                val item = getItem(position) as TextSharedItem
                holder.binding.item = item
                if (item.type == TextSharedItem.InputType.NUMBER) {
                    holder.binding.settingEditText.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                } else {
                    holder.binding.settingEditText.inputType = InputType.TYPE_CLASS_TEXT
                }
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedHolder {
        return when(viewType){
            CHECK -> {
                val binding = makeBinding<ItemSharedCheckboxBinding>(
                    parent,
                    R.layout.item_shared_checkbox
                )
                ItemSharedCheckboxHolder(binding)
            }
            SWITCH -> {
                val binding = makeBinding<ItemSharedSwitchBinding>(
                    parent,
                    R.layout.item_shared_switch
                )
                ItemSharedSwitchHolder(binding)
            }
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
            SELECTION -> {
                val binding = makeBinding<ItemSharedSelectionBinding>(
                    parent,
                    R.layout.item_shared_selection
                )
                ItemSettingSelectionHolder(binding)
            }
            TEXT -> {
                val binding = makeBinding<ItemSharedTextBinding>(parent, R.layout.item_shared_text)
                ItemSharedTextHolder(binding)
            }
            else -> throw IllegalArgumentException("not found")

        }
    }

    private fun <T : ViewDataBinding> makeBinding(parent: ViewGroup, @LayoutRes res: Int): T{
        return DataBindingUtil.inflate(LayoutInflater.from(parent.context), res, parent, false)
    }
}