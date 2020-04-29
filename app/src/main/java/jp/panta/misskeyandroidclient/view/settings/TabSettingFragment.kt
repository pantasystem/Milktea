package jp.panta.misskeyandroidclient.view.settings

import android.os.Bundle
import android.util.Log
import android.view.*
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
import jp.panta.misskeyandroidclient.setMenuTint
import jp.panta.misskeyandroidclient.viewmodel.setting.tab.SettingTab
import kotlinx.android.synthetic.main.fragment_tab_setting.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TabSettingFragment : Fragment(){

    private val defaultTabType = listOf(NoteType.HOME, NoteType.SOCIAL, NoteType.GLOBAL)

    val mSelectedListLiveData = MutableLiveData<List<SettingTab>>()

    val mSelectableListLiveData = MutableLiveData<List<SettingTab>>()

    var exSettings: List<NoteRequest.Setting>? = null

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
            override fun onClick(item: SettingTab) {
                val list = mSelectedListLiveData.value?: return
                val arrayList = ArrayList<SettingTab>(list)
                arrayList.remove(item)
                mSelectedListLiveData.value = arrayList
                mSelectableListLiveData.value = notSelectedSettings(arrayList)
            }
        })

        val selectableTabAdapter = NoteSettingListAdapter(diffUtilCallBack, false, object : NoteSettingListAdapter.ItemAddOrRemoveButtonClickedListener{
            override fun onClick(item: SettingTab) {
                if(item.type == NoteType.SEARCH || item.type == NoteType.SEARCH_HASH){

                    return
                }

                val list = mSelectedListLiveData.value?: return
                //val selectableList = mSelectableListLiveData.value?: return
                val arrayList = ArrayList<SettingTab>(list)
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




        /*miApplication.currentAccount.observe(viewLifecycleOwner, Observer { ar ->
            val list = if(ar.pages.isNullOrEmpty()){
                defaultTabVisibleSettings()
            }else{
                ar.pages.sortedBy {
                    it.weight
                }.map{nrt ->
                    SettingTab.FromSetting(nrt)
                }
            }
            exSettings = ar.pages
            val selectableList = notSelectedSettings(list)
            mSelectedListLiveData.postValue(list)
            mSelectableListLiveData.postValue(selectableList)
        })*/




        mSelectedListLiveData.observe(viewLifecycleOwner, Observer {
            selectedTabAdapter.submitList(it)
        })

        mSelectableListLiveData.observe(viewLifecycleOwner, Observer {
            selectableTabAdapter.submitList(it)
        })


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tab_setting_menu, menu)
        context?.setMenuTint(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save ->{
                saveTabs()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveTabs(){
        GlobalScope.launch{
            val miApplication = context?.applicationContext as MiApplication?
            //val dao = miApplication?.mNoteRequestSettingDao?: return@launch
            /*exSettings?.let{
                miApplication?.removeAllPagesInCurrentAccount(it)
            }*/

            val selectedList = mSelectedListLiveData.value?.mapNotNull {
                it.toSetting()
            } ?: return@launch


            selectedList.forEachIndexed { index, setting ->
                setting.weight = index
            }


            //miApplication?.replaceAllPagesInCurrentAccount(selectedList)
            Log.d("TabSettingFragment", "設定完了")
        }
    }


    private fun defaultTabVisibleSettings(): List<SettingTab>{
        return defaultTabType.map{
            SettingTab.FromType(it)
        }
    }

    private fun notSelectedSettings(selectedSettings: List<SettingTab>): List<SettingTab>{
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

    private fun getDefaultSettings(): List<SettingTab>{

        return NoteType.values().filter{
            it == NoteType.HOME || it == NoteType.LOCAL || it == NoteType.SOCIAL || it == NoteType.GLOBAL || it == NoteType.FAVORITE
        }.map{
            SettingTab.FromSetting(NoteRequest.Setting(type = it))
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

            val arrayList = ArrayList<SettingTab>(list)
            val data =arrayList.removeAt(from)
            arrayList.add(to, data)
            mSelectedListLiveData.value = arrayList

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //何もしない
        }
    }

    private val diffUtilCallBack  =object : DiffUtil.ItemCallback<SettingTab>(){
        override fun areContentsTheSame(
            oldItem: SettingTab,
            newItem: SettingTab
        ): Boolean {
            return oldItem.title.value == newItem.title.value && oldItem.type == newItem.type
        }

        override fun areItemsTheSame(oldItem: SettingTab, newItem: SettingTab): Boolean {
            return oldItem.title.value == newItem.title.value
        }
    }
}