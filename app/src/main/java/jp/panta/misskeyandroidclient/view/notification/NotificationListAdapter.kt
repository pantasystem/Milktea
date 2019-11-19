package jp.panta.misskeyandroidclient.view.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.databinding.ItemNotificationBinding
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData


class NotificationListAdapter(
    diffUtilCallBack: DiffUtil.ItemCallback<NotificationViewData>,
    val notesViewModel: NotesViewModel,
    private val lifecycleOwner: LifecycleOwner
    ) : ListAdapter<NotificationViewData, NotificationListAdapter.NotificationHolder>(diffUtilCallBack){
    class NotificationHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.binding.notesViewModel = notesViewModel
        holder.binding.notification = getItem(position)
        holder.binding.simpleNote

        val note = getItem(position).noteViewData
        note?: return

        val adapter = ReactionCountAdapter(
            object : DiffUtil.ItemCallback<Pair<String, Int>>(){
                override fun areContentsTheSame(
                    oldItem: Pair<String, Int>,
                    newItem: Pair<String, Int>
                ): Boolean {
                    return oldItem == newItem
                }

                override fun areItemsTheSame(
                    oldItem: Pair<String, Int>,
                    newItem: Pair<String, Int>
                ): Boolean {
                    return oldItem.first == newItem.first
                }
            }
            , note, notesViewModel)
        adapter.submitList(note.reactionCounts.value?.toList())
        note.reactionCounts.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toList())
        })
        holder.binding.simpleNote.reactionView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.binding.simpleNote.reactionView.adapter = adapter
        holder.binding.executePendingBindings()
        holder.binding.lifecycleOwner = lifecycleOwner

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding = DataBindingUtil.inflate<ItemNotificationBinding>(LayoutInflater.from(parent.context), R.layout.item_notification, parent, false)
        return NotificationHolder(binding)
    }
}