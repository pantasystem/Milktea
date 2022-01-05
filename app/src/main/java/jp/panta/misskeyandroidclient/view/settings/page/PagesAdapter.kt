package jp.panta.misskeyandroidclient.view.settings.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemPageSettingBinding
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageSettingViewModel

class PagesAdapter(private val pageSettingViewModel: PageSettingViewModel) : ListAdapter<Page, PagesAdapter.VH>(ItemDiffUtil()){

    class ItemDiffUtil : DiffUtil.ItemCallback<Page>(){
        override fun areContentsTheSame(oldItem: Page, newItem: Page): Boolean {
            return oldItem.title == newItem.title
                    && oldItem.accountId == newItem.accountId
                    && oldItem.pageId == newItem.pageId
        }

        override fun areItemsTheSame(oldItem: Page, newItem: Page): Boolean {
            // id はNullである可能性があるため全比較をしている
            return oldItem == newItem
        }
    }

    class VH(val binding: ItemPageSettingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = DataBindingUtil.inflate<ItemPageSettingBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_page_setting,
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding = holder.binding
        binding.page = getItem(position)
        binding.pageSettingAction = pageSettingViewModel

    }
}