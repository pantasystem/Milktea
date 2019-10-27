package jp.panta.misskeyandroidclient.view.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import kotlinx.android.synthetic.main.fragment_tab_setting.*
import java.io.Serializable

class TabSettingFragment : Fragment(){

    private val defaultTabType = listOf(NoteType.HOME, NoteType.SOCIAL, NoteType.GLOBAL)

    //private lateinit var mAdapter: NoteSettingListAdapter

    val mListLiveData = MutableLiveData<List<Serializable>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val miApplication = context?.applicationContext as MiApplication

        val adapter = NoteSettingListAdapter(object : DiffUtil.ItemCallback<Serializable>(){
            override fun areContentsTheSame(oldItem: Serializable, newItem: Serializable): Boolean {
                return if(oldItem is NoteRequest.Setting && newItem is NoteRequest.Setting){
                    return newItem.equals(oldItem)
                }else if(oldItem is SettingTitle && newItem is SettingTitle){
                    //oldItem.title == newItem.title
                    return oldItem.equals(newItem)
                }else{
                    false
                }
            }

            override fun areItemsTheSame(oldItem: Serializable, newItem: Serializable): Boolean {
                return if(oldItem is NoteRequest.Setting && newItem is NoteRequest.Setting){
                    return newItem.equals(oldItem)
                }else if(oldItem is SettingTitle && newItem is SettingTitle){
                    //oldItem.title == newItem.title
                    return oldItem.equals(newItem)
                }else{
                    false
                }
            }
        })

        setting_note_list_view.layoutManager = LinearLayoutManager(this.context)
        setting_note_list_view.adapter = adapter
        val touchHelper = ItemTouchHelper(ItemTouchCallBack())
        touchHelper.attachToRecyclerView(setting_note_list_view)
        setting_note_list_view.addItemDecoration(touchHelper)

        miApplication.noteRequestSettingDao?.findAll()?.observe(viewLifecycleOwner, Observer {
            val list = if(it.isNullOrEmpty()){
                makeList(defaultTabVisibleSettings())
            }else{
                makeList(it)
            }
            mListLiveData.postValue(list)

        })

        mListLiveData.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

    }

    private fun makeList(selectedList: List<NoteRequest.Setting>): List<Serializable>{
        val notSelectedList = notSelectedSettings(selectedList)
        val notSelected = SettingTitle("未選択")
        val selected = SettingTitle("選択済み")
        return ArrayList<Serializable>().apply{
            add(selected)
            addAll(selectedList)
            add(notSelected)
            addAll(notSelectedList)
        }

    }

    private fun defaultTabVisibleSettings(): List<NoteRequest.Setting>{
        return defaultTabType.map{
            NoteRequest.Setting(type = it)
        }
    }

    private fun notSelectedSettings(selectedSettings: List<NoteRequest.Setting>): List<NoteRequest.Setting>{
        return getDefaultSettings().filter{out ->
            selectedSettings.none { inner ->
                out.type == inner.type
            }
        }
    }

    private fun getDefaultSettings(): List<NoteRequest.Setting>{
        return listOf(NoteType.HOME, NoteType.LOCAL, NoteType.GLOBAL, NoteType.SOCIAL, NoteType.SEARCH, NoteType.SEARCH_HASH).map{
            if(it == NoteType.SEARCH){
                NoteRequest.Setting(type = it, query = "検索")
            }else if(it == NoteType.SEARCH_HASH){
                NoteRequest.Setting(type = it, query = "#検索")
            }else{
                NoteRequest.Setting(type = it)
            }


        }
    }

    inner class ItemTouchCallBack : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.ACTION_STATE_IDLE){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            val list = mListLiveData.value?: return false

            val arrayList = ArrayList<Serializable>(list)
            val data =arrayList.removeAt(from)
            arrayList.add(to, data)
            mListLiveData.value = arrayList

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //何もしない
        }
    }
}