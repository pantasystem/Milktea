package jp.panta.misskeyandroidclient.view.settings.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemSelectPageToAddBinding
import jp.panta.misskeyandroidclient.model.PageType
import jp.panta.misskeyandroidclient.viewmodel.setting.page.SelectPageTypeToAdd

class PageTypeListAdapter(private val selectPageTypeToAdd: SelectPageTypeToAdd) : ListAdapter<PageType, PageTypeListAdapter.VH>(ItemCallback()){

    class ItemCallback : DiffUtil.ItemCallback<PageType>(){
        override fun areContentsTheSame(oldItem: PageType, newItem: PageType): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: PageType, newItem: PageType): Boolean {
            return oldItem == newItem
        }
    }
    class VH(val binding: ItemSelectPageToAddBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.pageType = getItem(position)
        holder.binding.selectPageTypeToAdd = selectPageTypeToAdd
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = DataBindingUtil.inflate<ItemSelectPageToAddBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_select_page_to_add,
            parent,
            false
        )
        return VH(binding)
    }
}