package jp.panta.misskeyandroidclient.ui.notes.view.draft

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemDraftNotePollChoiceBinding


class PollChoiceAdapter : ListAdapter<String, PollChoiceAdapter.VH>(ChoiceItemCallback()){

    class VH(val binding: ItemDraftNotePollChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    class ChoiceItemCallback : DiffUtil.ItemCallback<String>(){

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.choice = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_draft_note_poll_choice,
                parent,
                false
            )
        )
    }
}

object DraftPollChoicesHelper{

    @JvmStatic
    @BindingAdapter("draftPollChoices")
    fun RecyclerView.setDraftPollChoices(draftPollChoices: List<String>?){
        if(draftPollChoices.isNullOrEmpty()){
            this.visibility = View.GONE
        }else{
            this.visibility = View.VISIBLE

            val adapter = PollChoiceAdapter()
            adapter.submitList(draftPollChoices)
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(this.context)
        }
    }
}