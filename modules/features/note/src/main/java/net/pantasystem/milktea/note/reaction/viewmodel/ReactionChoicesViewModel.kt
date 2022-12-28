package net.pantasystem.milktea.note.reaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.UserEmojiConfig
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryCount
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import javax.inject.Inject

@HiltViewModel
class ReactionChoicesViewModel @Inject constructor(
    accountStore: AccountStore,
    private val metaRepository: MetaRepository,
    private val reactionHistoryDao: ReactionHistoryRepository,
    private val userEmojiConfigRepository: UserEmojiConfigRepository,
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    private val meta = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        metaRepository.observe(ac.normalizedInstanceDomain)
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val reactionCount =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            reactionHistoryDao.observeSumReactions(ac.normalizedInstanceDomain)
        }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userSetting =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            userEmojiConfigRepository.observeByInstanceDomain(ac.normalizedInstanceDomain)
        }

    // 検索時の候補
    val uiState =
        combine(
            meta,
            accountStore.observeCurrentAccount,
            reactionCount,
            userSetting
        ) { meta, ac, counts, settings ->
            ReactionSelectionUiState(
                account = ac,
                meta = meta,
                reactionHistoryCounts = counts,
                userSettingReactions = settings,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ReactionSelectionUiState(null, null, emptyList(), emptyList())
        )

}

data class ReactionSelectionUiState(
    val account: Account?,
    val meta: Meta?,
    val reactionHistoryCounts: List<ReactionHistoryCount>,
    val userSettingReactions: List<UserEmojiConfig>,
) {


    val frequencyUsedReactionsV2: List<EmojiType> by lazy {
        reactionHistoryCounts.map {
            it.reaction
        }.mapNotNull { reaction ->
            EmojiType.from(meta?.emojis, reaction)
        }.distinct()
    }

    val all: List<EmojiType> by lazy {
        LegacyReaction.defaultReaction.map {
            EmojiType.Legacy(it)
        } + (meta?.emojis?.map {
            EmojiType.CustomEmoji(it)
        } ?: emptyList())
    }


    val userSettingEmojis: List<EmojiType> by lazy {
        userSettingReactions.mapNotNull { setting ->
            EmojiType.from(meta?.emojis, setting.reaction)
        }.ifEmpty {
            LegacyReaction.defaultReaction.map {
                EmojiType.Legacy(it)
            }
        }
    }


    fun getCategoryBy(category: String): List<EmojiType> {
        return meta?.emojis?.filter {
            it.category == category

        }?.map {
            EmojiType.CustomEmoji(it)
        } ?: emptyList()
    }

}

sealed interface EmojiType {
    data class Legacy(val type: String) : EmojiType
    data class CustomEmoji(val emoji: Emoji) : EmojiType
    data class UtfEmoji(val code: String) : EmojiType
    companion object
}

fun EmojiType.toTextReaction(): String {
    return when (val type = this) {
        is EmojiType.CustomEmoji -> {
            ":${type.emoji.name}:"
        }
        is EmojiType.Legacy -> {
            type.type
        }
        is EmojiType.UtfEmoji -> {
            type.code
        }
    }
}


fun EmojiType.Companion.from(emojis: List<Emoji>?, reaction: String): EmojiType? {
    return if (reaction.codePointCount(0, reaction.length) == 1) {
        EmojiType.UtfEmoji(reaction)
    } else if (reaction.startsWith(":") && reaction.endsWith(":") && reaction.contains(
            "@"
        )
    ) {
        val nameOnly = reaction.replace(":", "")
        (emojis?.firstOrNull {
            it.name == nameOnly
        } ?: (nameOnly.split("@")[0]).let { name ->
            emojis?.firstOrNull {
                it.name == name
            }
        })?.let {
            EmojiType.CustomEmoji(it)
        }
    } else if (LegacyReaction.reactionMap[reaction] != null) {
        EmojiType.Legacy(reaction)
    } else {
        emojis?.firstOrNull {
            it.name == reaction.replace(":", "")
        }?.let {
            EmojiType.CustomEmoji(it)
        }
    }
}