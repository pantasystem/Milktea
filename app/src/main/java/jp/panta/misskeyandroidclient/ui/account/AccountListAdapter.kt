package jp.panta.misskeyandroidclient.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemAccountBinding
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewData
import net.pantasystem.milktea.common_viewmodel.viewmodel.AccountViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AccountListAdapter constructor(diff: DiffUtil.ItemCallback<AccountViewData>, val accountViewModel: AccountViewModel, val lifecycleOwner: LifecycleOwner) : ListAdapter<AccountViewData, AccountListAdapter.AccountViewHolder>(diff){
    class AccountViewHolder(val binding: ItemAccountBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.binding.accountViewData = getItem(position)
        holder.binding.accountViewModel = accountViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = DataBindingUtil.inflate<ItemAccountBinding>(LayoutInflater.from(parent.context), R.layout.item_account, parent, false)
        return AccountViewHolder(binding)
    }
}