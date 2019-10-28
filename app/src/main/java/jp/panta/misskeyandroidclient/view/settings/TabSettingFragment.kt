package jp.panta.misskeyandroidclient.view.settings

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class TabSettingFragment : Fragment(){

    private val defaultTabType = listOf(NoteType.HOME, NoteType.SOCIAL, NoteType.GLOBAL)

    val mSelectedListLiveData = MutableLiveData<List<NoteRequest.Setting>>()

    val mSelectableListLiveData = MutableLiveData<List<NoteRequest.Setting>>()

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

        val selectedTabAdapter = NoteSettingListAdapter(diffUtilCallBack,true, object : NoteSettingListAdapter.ItemAddOrRemoveButtonClickedListener{
            override fun onClick(item: NoteRequest.Setting) {
                val list = mSelectedListLiveData.value?: return
                val arrayList = ArrayList<NoteRequest.Setting>(list)
                arrayList.remove(item)
                mSelectedListLiveData.value = arrayList
                mSelectableListLiveData.value = notSelectedSettings(arrayList)
            }
        })

        val selectableTabAdapter = NoteSettingListAdapter(diffUtilCallBack, false, object : NoteSettingListAdapter.ItemAddOrRemoveButtonClickedListener{
            override fun onClick(item: NoteRequest.Setting) {
                when(item.type){
                    NoteType.SEARCH, NoteType.SEARCH_HASH -> return
                }
                val list = mSelectedListLiveData.value?: return
                //val selectableList = mSelectableListLiveData.value?: return
                val arrayList = ArrayList<NoteRequest.Setting>(list)
                arrayList.add(item)
                mSelectedListLiveData.value = arrayList
                mSelectableListLiveData.value = notSelectedSettings(arrayList)
            }
        })



        selected_tab_list.layoutManager = LinearLayoutManager(this.context)
        selected_tab_list.adapter = selectedTabAdapter
        val touchHelper = ItemTouchHelper(ItemTouchCallBack())
        touchHelper.attachToRecyclerView(selected_tab_list)
        selected_tab_list.addItemDecoration(touchHelper)

        selectable_tab_list.layoutManager = LinearLayoutManager(this.context)
        selectable_tab_list.adapter = selectableTabAdapter


        miApplication.noteRequestSettingDao?.findAll()?.observe(viewLifecycleOwner, Observer {
            val list = if(it.isNullOrEmpty()){
                defaultTabVisibleSettings()
            }else{
                it
            }
            val selectableList = notSelectedSettings(list)
            mSelectedListLiveData.postValue(list)
            mSelectableListLiveData.postValue(selectableList)

        })




        mSelectedListLiveData.observe(viewLifecycleOwner, Observer {
            selectedTabAdapter.submitList(it)
        })

        mSelectableListLiveData.observe(viewLifecycleOwner, Observer {
            selectableTabAdapter.submitList(it)
        })

        save_setting.setOnClickListener {
            saveTabs()
        }

    }

    private fun saveTabs(){
        GlobalScope.launch{
            val miApplication = context?.applicationContext as MiApplication?
            val dao = miApplication?.noteRequestSettingDao?: return@launch

            val selectedList = mSelectedListLiveData.value?: return@launch
            dao.deleteAll()

            for(n in 0.until(selectedList.size)){
                selectedList[n].id = n.toLong()
            }
            dao.insertAll(selectedList)
            Log.d("TabSettingFragment", "設定完了")
        }
    }


    private fun defaultTabVisibleSettings(): List<NoteRequest.Setting>{
        return defaultTabType.map{
            NoteRequest.Setting(type = it)
        }
    }

    private fun notSelectedSettings(selectedSettings: List<NoteRequest.Setting>): List<NoteRequest.Setting>{
        return getDefaultSettings().filter{out ->
            if(out.type == NoteType.SEARCH_HASH || out.type == NoteType.SEARCH){
                true
            }else{
                selectedSettings.none { inner ->
                    out.type == inner.type
                }
            }


        }
    }

    private fun getDefaultSettings(): List<NoteRequest.Setting>{
        return NoteType.values().map{
            if(it == NoteType.SEARCH){
                NoteRequest.Setting(type = it, query = "検索")
            }else if(it == NoteType.SEARCH_HASH){
                NoteRequest.Setting(type = it, query = "#検索")
            }else if(it == NoteType.USER){
                NoteRequest.Setting(type = it)
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
            val list = mSelectedListLiveData.value?: return false

            val arrayList = ArrayList<NoteRequest.Setting>(list)
            val data =arrayList.removeAt(from)
            arrayList.add(to, data)
            mSelectedListLiveData.value = arrayList

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //何もしない
        }
    }

    private val diffUtilCallBack  =object : DiffUtil.ItemCallback<NoteRequest.Setting>(){
        override fun areContentsTheSame(
            oldItem: NoteRequest.Setting,
            newItem: NoteRequest.Setting
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: NoteRequest.Setting,
            newItem: NoteRequest.Setting
        ): Boolean {
            return oldItem == newItem
        }
    }
}