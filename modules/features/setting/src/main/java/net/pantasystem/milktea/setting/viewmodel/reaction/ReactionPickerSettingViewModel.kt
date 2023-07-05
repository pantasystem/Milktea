package net.pantasystem.milktea.setting.viewmodel.reaction

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.ReactionPickerType
import javax.inject.Inject

@HiltViewModel
class ReactionPickerSettingViewModel @Inject constructor(
    private val reactionUserSettingDao: ReactionUserSettingDao,
    private val settingStore: SettingStore,
    val accountStore: AccountStore,
    private val configRepository: LocalConfigRepository,
) : ViewModel(), ReactionSelection {


    companion object

    var reactionPickerType = settingStore.reactionPickerType
        private set
    val reactionSettingsList = MutableLiveData<List<ReactionUserSetting>>()
    val reactionSelectEvent = EventBus<ReactionUserSetting>()

    private var mExistingSettingList: List<ReactionUserSetting>? = null
    private val mReactionSettingReactionNameMap = LinkedHashMap<String, ReactionUserSetting>()

    val config = configRepository.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        configRepository.get().getOrNull() ?: DefaultConfig.config
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            accountStore.observeCurrentAccount.filterNotNull().collect {
                loadSetReactions(it)
            }
        }

    }

    private fun loadSetReactions(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawSettings = reactionUserSettingDao
                    .findByInstanceDomain(account.normalizedInstanceUri)
                mExistingSettingList = rawSettings ?: emptyList()
                var settingReactions = rawSettings
                    ?: LegacyReaction.defaultReaction.mapIndexed { index, str ->
                        toReactionUserSettingFromTextTypeReaction(account, index, str)
                    }
                if (settingReactions.isEmpty()) {
                    settingReactions =
                        LegacyReaction.defaultReaction.mapIndexed { index, str ->
                            toReactionUserSettingFromTextTypeReaction(account, index, str)
                        }
                }
                mReactionSettingReactionNameMap.clear()
                mReactionSettingReactionNameMap.putAll(settingReactions.map {
                    it.reaction to it
                })
                reactionSettingsList.postValue(settingReactions)


            } catch (e: Exception) {
                Log.e("ReactionPickerSettingVM", "load set reaction error", e)
            }
        }
    }

    fun save() {
        val userSettings = this.reactionSettingsList.value
        userSettings?.forEachIndexed { index, reactionUserSetting ->
            reactionUserSetting.weight = index
        }
        val ex = mExistingSettingList ?: emptyList()
        val removed = ex.filter { out ->
            userSettings?.any { inner ->
                out.instanceDomain == inner.instanceDomain && out.reaction == inner.reaction
            } == false
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reactionUserSettingDao.deleteAll(removed)
                userSettings?.let {
                    reactionUserSettingDao.insertAll(userSettings)
                }
            } catch (e: Exception) {
                Log.e("ReactionPickerSettingVM", "save error", e)
            }
        }
    }

    // delete reaction
    override fun selectReaction(reaction: String) {
        mReactionSettingReactionNameMap[reaction]?.let {
            reactionSelectEvent.event = it
        }
    }

    fun deleteReaction(reaction: String) {
        mReactionSettingReactionNameMap.remove(reaction)
        reactionSettingsList.postValue(
            mReactionSettingReactionNameMap.values.toList()
        )
    }

    fun addReaction(reaction: String) {
        val account = accountStore.currentAccount ?: return
        mReactionSettingReactionNameMap[reaction] =
            ReactionUserSetting(
                reaction,
                account.normalizedInstanceUri,
                mReactionSettingReactionNameMap.size
            )
        reactionSettingsList.postValue(mReactionSettingReactionNameMap.values.toList())
    }

    fun putSortedList(list: List<ReactionUserSetting>) {
        mReactionSettingReactionNameMap.clear()
        mReactionSettingReactionNameMap.putAll(list.map {
            it.reaction to it
        })
        reactionSettingsList.postValue(mReactionSettingReactionNameMap.values.toList())
    }

    fun setReactionPickerType(type: ReactionPickerType) {
        settingStore.reactionPickerType = type
        reactionPickerType = type
    }

    fun onEmojiSizeSelected(size: Int) {
        viewModelScope.launch {
            val c = configRepository.get().getOrNull() ?: DefaultConfig.config
            configRepository.save(
                c.copy(
                    emojiPickerEmojiDisplaySize = size
                )
            )
        }
    }

    private fun toReactionUserSettingFromTextTypeReaction(
        account: Account,
        index: Int,
        reaction: String,
    ): ReactionUserSetting {
        return ReactionUserSetting(
            reaction,
            account.normalizedInstanceUri,
            index
        )
    }
}
