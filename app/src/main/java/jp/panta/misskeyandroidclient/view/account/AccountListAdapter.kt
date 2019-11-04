package jp.panta.misskeyandroidclient.view.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemAccountBinding
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewData
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel

class AccountListAdapter(diff: DiffUtil.ItemCallback<AccountViewData>, val accountViewModel: AccountViewModel) : ListAdapter<AccountViewData, AccountListAdapter.AccountViewHolder>(diff){
    class AccountViewHolder(val binding: ItemAccountBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.binding.accountViewData = getItem(position)
        holder.binding.accountViewModel = accountViewModel
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = DataBindingUtil.inflate<ItemAccountBinding>(LayoutInflater.from(parent.context), R.layout.item_account, parent, false)
        return AccountViewHolder(binding)
    }
}