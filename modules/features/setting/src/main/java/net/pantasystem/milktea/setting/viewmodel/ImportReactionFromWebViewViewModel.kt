package net.pantasystem.milktea.setting.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.setting.WebClientBaseCache
import javax.inject.Inject

@HiltViewModel
class ImportReactionFromWebViewViewModel @Inject constructor(
    val accountStore: AccountStore
) : ViewModel() {


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
        }

    }
}

