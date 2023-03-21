package net.pantasystem.milktea.setting.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.register.WebClientBaseRequest
import net.pantasystem.milktea.api.misskey.register.WebClientRegistries
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject

@HiltViewModel
class ImportReactionFromWebViewViewModel @Inject constructor(
    val accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    private val reactionUserSettingDao: ReactionUserSettingDao,
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
) : ViewModel() {

    val logger = loggerFactory.create("IRFWVViewModel")

    private val _reactions = MutableStateFlow(listOf<String>())
    val reactions: StateFlow<List<String>> = _reactions

    fun onOverwriteButtonClicked() {
        viewModelScope.launch {
            try {

                val account = accountRepository.getCurrentAccount().getOrThrow()
                reactionUserSettingDao.deleteAll(reactionUserSettingDao.findByInstanceDomain(account.normalizedInstanceUri) ?: emptyList())
                val settings = reactions.value.mapIndexed { i, reaction ->
                    ReactionUserSetting(
                        reaction,
                        account.normalizedInstanceUri,
                        i
                    )
                }
                reactionUserSettingDao.insertAll(settings)
            } catch (e: Exception) {
                Log.e("ReactionPickerSettingVM", "save error", e)
            }
        }
    }

    fun onGotWebClientToken(account: Account, token: String?) {
        if (token == null) {
            return
        }

        viewModelScope.launch {
            getReactions(account, token).onSuccess {
                _reactions.value = it.reactions
            }.onFailure {
                logger.error("fetch reactions error", it)
            }
        }
    }

    private suspend fun getReactions(account: Account, token: String): Result<WebClientRegistries> = runCancellableCatching{
        withContext(Dispatchers.IO) {
            val body = misskeyAPIProvider.get(account).getReactionsFromGetAll(WebClientBaseRequest(
                i = token,
                scope = listOf("client", "base")
            )).throwIfHasError().body()
            requireNotNull(body)
        }
    }
}

