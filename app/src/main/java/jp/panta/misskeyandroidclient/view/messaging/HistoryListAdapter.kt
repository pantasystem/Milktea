package jp.panta.misskeyandroidclient.view.messaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemMessagingHistoryBinding
import jp.panta.misskeyandroidclient.viewmodel.messaging.HistoryViewData
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageHistoryViewModel

class HistoryListAdapter(
    itemCallback: DiffUtil.ItemCallback<HistoryViewData>,
    private val historyViewModel: MessageHistoryViewModel,
    private val lifecycleOwner: LifecycleOwner
    ) : ListAdapter<HistoryViewData, HistoryListAdapter.HistoryViewHolder>(itemCallback){
    class HistoryViewHolder(val binding: ItemMessagingHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = DataBindingUtil.inflate<ItemMessagingHistoryBinding>(LayoutInflater.from(parent.context), R.layout.item_messaging_history, parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.binding.historyViewData = getItem(position)
        holder.binding.historyViewModel = historyViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }
}