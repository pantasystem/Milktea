package net.pantasystem.milktea.setting.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSetting
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.setting.WebClientBaseCache
import javax.inject.Inject

@HiltViewModel
class ImportReactionFromWebViewViewModel @Inject constructor(
    val accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    private val reactionUserSettingDao: ReactionUserSettingDao,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val logger = loggerFactory.create("IRFWVViewModel")

    private val _reactions = MutableStateFlow(listOf<String>())
    val reactions: StateFlow<List<String>> = _reactions

    fun onLocalStorageCacheLoaded(result: String) {
        val decoder = Json {
            ignoreUnknownKeys = true
        }
        runCatching {
            decoder.decodeFromString<WebClientBaseCache>(result)
        }.onSuccess {
            _reactions.value = it.reactions
        }.onFailure {
            logger.error("onLocalStorageCacheLoaded decode json error", it)
        }

    }

    fun onOverwriteButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val account = accountRepository.getCurrentAccount().getOrThrow()
                reactionUserSettingDao.deleteAll(reactionUserSettingDao.findByInstanceDomain(account.instanceDomain) ?: emptyList())
                val settings = reactions.value.mapIndexed { i, reaction ->
                    ReactionUserSetting(
                        reaction,
                        account.instanceDomain,
                        i
                    )
                }
                reactionUserSettingDao.insertAll(settings)
            } catch (e: Exception) {
                Log.e("ReactionPickerSettingVM", "save error", e)
            }
        }
    }
}

