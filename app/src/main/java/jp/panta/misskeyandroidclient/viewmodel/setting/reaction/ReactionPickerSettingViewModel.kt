package jp.panta.misskeyandroidclient.viewmodel.setting.reaction

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionUserSetting
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.settings.ReactionPickerType
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class ReactionPickerSettingViewModel(
    private val accountRelation: AccountRelation,
    private val reactionUserSettingDao: ReactionUserSettingDao,
    private val miCore: MiCore
) : ViewModel(){

    class Factory{

    }

    val reactionSettingsText = MutableLiveData<String>()
    val reactionPickerType = MutableLiveData<ReactionPickerType>()

    private val mEmojiPattern = Pattern.compile("""\A:([a-zA-Z0-9+\-_]+):""")
    private val mDefaultReactionPattern = Pattern.compile("""([a-z]+)""")

    init{
        loadSetReactions()
    }

    private fun loadSetReactions(){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val reactionSettingsBuilder = StringBuilder()
                var settingReactions = reactionUserSettingDao
                    .findByInstanceDomain(accountRelation.getCurrentConnectionInformation()?.instanceBaseUrl!!)
                    ?.map{ it.reaction }
                    ?: ReactionResourceMap.defaultReaction
                if(settingReactions.isEmpty()){
                    settingReactions = ReactionResourceMap.defaultReaction
                }
                settingReactions.forEach{ reaction ->
                    reactionSettingsBuilder.append(reaction)
                }
                reactionSettingsText.postValue(
                    reactionSettingsBuilder.toString()
                )


            }catch(e: Exception){
                Log.e("ReactionPickerSettingVM", "load set reaction error", e)
            }
        }
    }
    fun save(){
        val instance = accountRelation.getCurrentConnectionInformation()?.instanceBaseUrl!!

        val instanceEmojis = miCore.getCurrentInstanceMeta()?.emojis?.map{
            it.name to it
        }?.toMap()?: emptyMap()
        val defaultReactions = ReactionResourceMap.reactionMap
        val fieldReactions = reactionSettingsText.value?: ""
        val reactionInitials = ReactionResourceMap.defaultReaction.map{
            it.first()
        }.toSet()

        val reactionUserSettings = ArrayList<ReactionUserSetting>()
        var position = 0
        var beforeCheckPosition = position
        var isCheckingConstantReaction = false
        var checkingTextBuilder = StringBuilder()
        while(position < fieldReactions.length){
            val c = fieldReactions[position]
            if(isCheckingConstantReaction){
                checkingTextBuilder.append(c)
                val checking = checkingTextBuilder.toString()
                if(!mDefaultReactionPattern.matcher(checking).find()){
                    position = beforeCheckPosition + 1
                    checkingTextBuilder = StringBuilder()
                    isCheckingConstantReaction = false
                    continue
                }
                if(defaultReactions[checking] != null){
                    isCheckingConstantReaction = false
                    checkingTextBuilder = StringBuilder()
                    reactionUserSettings.add(
                        ReactionUserSetting(
                            reaction = checking,
                            instanceDomain = instance,
                            weight = reactionUserSettings.size
                        )
                    )
                }
            }else{
                when {
                    reactionInitials.contains(c) -> {
                        // constant reaction
                        isCheckingConstantReaction = true
                        beforeCheckPosition = position
                        checkingTextBuilder.append(c)
                    }
                    c == ':' -> {
                        // custom emoji
                        val matcher = mEmojiPattern.matcher(fieldReactions.substring(position, fieldReactions.length))
                        if(matcher.find()){
                            val reaction = matcher.group(1)
                            if(instanceEmojis[reaction] != null){
                                reactionUserSettings.add(
                                    ReactionUserSetting(
                                        reaction = matcher.group(),
                                        instanceDomain = instance,
                                        weight = reactionUserSettings.size
                                    )
                                )
                                // 強制的に加算される分を加味して一つ減らす
                                position += matcher.end() - 1
                            }
                        }
                    }
                    else -> {
                        // emoji
                        reactionUserSettings.add(
                            ReactionUserSetting(
                                reaction = c.toString(),
                                instanceDomain = instance,
                                weight = reactionUserSettings.size
                            )
                        )
                    }
                }
            }
            position ++
        }
        Log.d("ReactionPickerSettingVM", "selected: $reactionUserSettings")
    }
}