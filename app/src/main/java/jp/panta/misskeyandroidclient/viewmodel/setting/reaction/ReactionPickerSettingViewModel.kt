package jp.panta.misskeyandroidclient.viewmodel.setting.reaction

import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionUserSetting
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.reaction.ReactionSelection
import jp.panta.misskeyandroidclient.model.settings.ReactionPickerType
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class ReactionPickerSettingViewModel(
    private val accountRelation: AccountRelation,
    private val reactionUserSettingDao: ReactionUserSettingDao,
    private val settingStore: SettingStore,
    private val miCore: MiCore
) : ViewModel(), ReactionSelection{

    @Suppress("UNCHECKED_CAST")
    class Factory(val ar: AccountRelation, val miApplication: MiApplication) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val settingStore = SettingStore(
                PreferenceManager.getDefaultSharedPreferences(miApplication)
            )
            return ReactionPickerSettingViewModel(ar, miApplication.reactionUserSettingDao, settingStore, miApplication) as T
        }
    }

    var reactionPickerType = settingStore.reactionPickerType
        private set
    val reactionSettingsList = MutableLiveData<List<ReactionUserSetting>>()
    val reactionSelectEvent = EventBus<ReactionUserSetting>()

    private var mExistingSettingList: List<ReactionUserSetting>? = null
    private val mReactionSettingReactionNameMap = LinkedHashMap<String, ReactionUserSetting>()


    private val mEmojiPattern = Pattern.compile("""\A:([a-zA-Z0-9+\-_]+):""")
    private val mDefaultReactionPattern = Pattern.compile("""([a-z^\S]+)""")

    init{
        loadSetReactions()
    }

    private fun loadSetReactions(){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val rawSettings = reactionUserSettingDao
                    .findByInstanceDomain(accountRelation.getCurrentConnectionInformation()?.instanceBaseUrl!!)
                mExistingSettingList = rawSettings?: emptyList()
                var settingReactions = rawSettings
                    ?: ReactionResourceMap.defaultReaction.mapIndexed(::toReactionUserSettingFromTextTypeReaction)
                if(settingReactions.isEmpty()){
                    settingReactions = ReactionResourceMap.defaultReaction.mapIndexed(::toReactionUserSettingFromTextTypeReaction)
                }
                mReactionSettingReactionNameMap.clear()
                mReactionSettingReactionNameMap.putAll(settingReactions.map{
                    it.reaction to it
                })
                reactionSettingsList.postValue(settingReactions)


            }catch(e: Exception){
                Log.e("ReactionPickerSettingVM", "load set reaction error", e)
            }
        }
    }

    fun save(){
        val userSettings = this.reactionSettingsList.value
        userSettings?.forEachIndexed { index, reactionUserSetting ->
            reactionUserSetting.weight = index
        }
        val ex = mExistingSettingList?: emptyList()
        val removed = ex.filter{ out ->
            userSettings?.any{ inner ->
                out.instanceDomain == inner.instanceDomain && out.reaction == inner.reaction
            } == false
        }
        viewModelScope.launch(Dispatchers.IO){
            try{
                reactionUserSettingDao.deleteAll(removed)
                userSettings?.let{
                    reactionUserSettingDao.insertAll(userSettings)
                }
            }catch(e: Exception){
                Log.e("ReactionPickerSettingVM", "save error", e)
            }
        }
    }

    // delete reaction
    override fun selectReaction(reaction: String) {
        mReactionSettingReactionNameMap[reaction]?.let{
            reactionSelectEvent.event = it
        }
    }

    fun deleteReaction(reaction: String) {
        mReactionSettingReactionNameMap.remove(reaction)
        reactionSettingsList.postValue(
            mReactionSettingReactionNameMap.values.toList()
        )
    }

    fun addReaction(reaction: String){
        mReactionSettingReactionNameMap[reaction] = ReactionUserSetting(
            reaction,
            accountRelation.getCurrentConnectionInformation()?.instanceBaseUrl!!,
            mReactionSettingReactionNameMap.size
        )
        reactionSettingsList.postValue(mReactionSettingReactionNameMap.values.toList())
    }

    fun putSortedList(list: List<ReactionUserSetting>){
        mReactionSettingReactionNameMap.clear()
        mReactionSettingReactionNameMap.putAll(list.map{
            it.reaction to it
        })
        reactionSettingsList.postValue(mReactionSettingReactionNameMap.values.toList())
    }

    fun setReactionPickerType(type: ReactionPickerType){
        settingStore.reactionPickerType = type
        reactionPickerType = type
    }

    private fun toReactionUserSettingFromTextTypeReaction(index: Int, reaction: String): ReactionUserSetting{
        return ReactionUserSetting(reaction, accountRelation.getCurrentConnectionInformation()?.instanceBaseUrl!!, index)
    }
}