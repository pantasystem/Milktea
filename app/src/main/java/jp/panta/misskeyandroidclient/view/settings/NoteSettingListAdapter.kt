package jp.panta.misskeyandroidclient.view.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.view.notes.TabFragment
import kotlinx.android.synthetic.main.item_note_setting.view.*
import kotlinx.android.synthetic.main.item_note_setting_bound.view.*
import java.io.Serializable
import java.lang.ClassCastException
import java.lang.IllegalArgumentException

class NoteSettingListAdapter(diffUtil: DiffUtil.ItemCallback<NoteRequest.Setting>, private val isSelected: Boolean, private val listener: ItemAddOrRemoveButtonClickedListener) : ListAdapter<NoteRequest.Setting, NoteSettingListAdapter.NoteSettingViewHolder>(diffUtil){

    abstract class NoteSettingViewHolderBase(view: View) : RecyclerView.ViewHolder(view)
    inner class NoteSettingViewHolder(private val view: View) : NoteSettingViewHolderBase(view){
        fun onBind(item: NoteRequest.Setting, isSelected: Boolean){
            view.setting_title.text = TabFragment.localizationTitle(item)
            if(isSelected){
                view.add_or_remove_icon.setImageResource(R.drawable.ic_remove_circle_outline_black_24dp)
            }else{
                view.add_or_remove_icon.setImageResource(R.drawable.ic_add_circle_outline_black_24dp)
            }
            view.add_or_remove_icon.setOnClickListener {
                listener.onClick(item)
            }
        }
    }


    override fun onBindViewHolder(holder: NoteSettingViewHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item, isSelected)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteSettingViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note_setting, parent, false)
        return NoteSettingViewHolder(view)

    }

    interface ItemAddOrRemoveButtonClickedListener{
        fun onClick(item: NoteRequest.Setting)
    }
}