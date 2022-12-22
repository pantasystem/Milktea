package net.pantasystem.milktea.note.reaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import javax.inject.Inject

@HiltViewModel
class ReactionSelectionDialogViewModel @Inject constructor(
    val accountStore: AccountStore,
    val metaRepository: MetaRepository,
) : ViewModel() {

    val searchWord = MutableStateFlow("")

    val isSearchMode = searchWord.map {
        it.isNotBlank()
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEmojis = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        metaRepository.observe(ac.normalizedInstanceDomain)
    }.filterNotNull().mapNotNull {
        it.emojis
    }.flatMapLatest { emojis ->
        searchWord.map {
            emojis.filterEmojiBy(it)
        }
    }.map { emojis ->
        emojis.map {
            EmojiType.CustomEmoji(it)
        }
    }.distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        metaRepository.observe(it.normalizedInstanceDomain)
    }.map{
        it.makeTabItems()
    }.distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}


sealed interface TabType {
    object UserCustom : TabType
    object OftenUse : TabType
    object All : TabType
    data class Category(val name: String) : TabType
}

fun Meta?.makeTabItems(): List<TabType> {
    val categoryNames = this?.emojis?.filter {
        it.category != null
    }?.groupBy {
        it.category ?: ""
    }?.keys ?: emptySet()

    return listOf(
        TabType.UserCustom,
        TabType.OftenUse,
        TabType.All
    ) + categoryNames.map {
        TabType.Category(it)
    }
}

fun List<Emoji>.filterEmojiBy(word: String): List<Emoji> {
    val w = word.replace(":", "")
    return filter { emoji ->
        emoji.name.contains(w)
                || emoji.aliases?.any { alias ->
            alias.startsWith(w)
        } ?: false
    }
}