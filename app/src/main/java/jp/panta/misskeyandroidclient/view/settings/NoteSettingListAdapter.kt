package jp.panta.misskeyandroidclient.view.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteSettingBinding
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.view.notes.TabFragment
import jp.panta.misskeyandroidclient.viewmodel.setting.tab.SettingTab
import kotlinx.android.synthetic.main.item_note_setting.view.*
import kotlinx.android.synthetic.main.item_note_setting_bound.view.*
import java.io.Serializable
import java.lang.ClassCastException
import java.lang.IllegalArgumentException

class NoteSettingListAdapter(
    diffUtil: DiffUtil.ItemCallback<SettingTab>,
    private val isSelected: Boolean,
    private val listener: ItemAddOrRemoveButtonClickedListener
) : ListAdapter<SettingTab, NoteSettingListAdapter.ViewHolder>(diffUtil){

    class ViewHolder(val binding: ItemNoteSettingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.isSelecting = !isSelected
        holder.binding.settingTab = getItem(position)
        holder.binding.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ItemNoteSettingBinding>(LayoutInflater.from(parent.context), R.layout.item_note_setting, parent, false)
        return ViewHolder(binding)
    }

    interface ItemAddOrRemoveButtonClickedListener{
        fun onClick(item: SettingTab)
    }
}