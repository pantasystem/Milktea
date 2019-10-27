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

class NoteSettingListAdapter(diffUtil: DiffUtil.ItemCallback<Serializable>) : ListAdapter<Serializable, NoteSettingListAdapter.NoteSettingViewHolderBase>(diffUtil){
    companion object{
        private const val TYPE_SETTING = 0
        private const val TYPE_TITLE = 1
    }

    abstract class NoteSettingViewHolderBase(view: View) : RecyclerView.ViewHolder(view)
    class NoteSettingViewHolder(private val view: View) : NoteSettingViewHolderBase(view){
        fun onBind(item: NoteRequest.Setting){
            view.setting_title.text = TabFragment.localizationTitle(item)
        }
    }
    class NoteSettingBoundViewHolder(private val view: View) : NoteSettingViewHolderBase(view){
        fun onBind(item: SettingTitle){
            view.setting_bound_title.text = item.title
        }
    }



    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NoteRequest.Setting -> TYPE_SETTING
            is SettingTitle -> TYPE_TITLE
            else -> throw ClassCastException("NoteRequest.Setting.class, SettingTitleのみしか許可されていません")
        }
    }
    override fun onBindViewHolder(holder: NoteSettingViewHolderBase, position: Int) {
        val item = getItem(position)
        if(item is SettingTitle && holder is NoteSettingBoundViewHolder){
            holder.onBind(item)
        }else if(item is NoteRequest.Setting && holder is NoteSettingViewHolder){
            holder.onBind(item)
        }else{
            Log.e("NoteSettingListAdapter", "不明な型ですbindに失敗しました")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteSettingViewHolderBase {
        return when(viewType){
            TYPE_SETTING ->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note_setting, parent, false)
                NoteSettingViewHolder(view)
            }
            TYPE_TITLE -> NoteSettingBoundViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_note_setting_bound, parent, false))
            else -> throw IllegalArgumentException("ViewTypeが異常ですNoteSettingListAdapterに問題があります")
        }

    }
}