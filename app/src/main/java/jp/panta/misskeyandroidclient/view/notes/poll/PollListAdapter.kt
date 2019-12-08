package jp.panta.misskeyandroidclient.view.notes.poll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemChoiceBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.poll.PollViewData

class PollListAdapter(
    val poll: PollViewData,
    val notesViewModel: NotesViewModel,
    val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<PollListAdapter.ChoiceHolder>(){
    class ChoiceHolder(val binding: ItemChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return poll.choices.size
    }

    override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
        /*holder.binding.apply{
            poll = poll
            choice = poll!!.choices[position]
        }*/
        holder.binding.poll = poll
        holder.binding.choice = poll.choices[position]

        holder.binding.notesViewModel = notesViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemChoiceBinding>(inflater, R.layout.item_choice, parent, false)
        return ChoiceHolder(binding)
    }
}