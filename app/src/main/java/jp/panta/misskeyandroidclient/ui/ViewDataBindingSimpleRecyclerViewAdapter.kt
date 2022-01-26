package jp.panta.misskeyandroidclient.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ViewDataBindingSimpleRecyclerViewAdapter<T, VDB: ViewDataBinding>(
    private val onBind: (binding: VDB, item: T)-> Unit,
    @LayoutRes val layoutRes: Int,
    val lifecycleOwner: LifecycleOwner,
    key: (item: T)-> Any,
    onDeepEqual: (new: T, old: T)-> Boolean = { i, n -> i == n },
) : ListAdapter<T, ViewDataBindingSimpleRecyclerViewAdapter.ViewBindingViewHolder<VDB>>(
    object : DiffUtil.ItemCallback<T>() {
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return onDeepEqual(newItem, oldItem)
        }

        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return key(oldItem) == key(newItem)
        }
    }
) {

    class ViewBindingViewHolder<VDB : ViewDataBinding>(val binding: VDB) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: ViewBindingViewHolder<VDB>, position: Int) {
        onBind(holder.binding, getItem(position))
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingViewHolder<VDB> {
        val binding = DataBindingUtil.inflate<VDB>(LayoutInflater.from(parent.context), layoutRes, parent, false)
        return ViewBindingViewHolder(binding)
    }
}