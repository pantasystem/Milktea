package net.pantasystem.milktea.note.reaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
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
    private val reactionUserSettingDao: ReactionUserSettingDao,
) : ViewModel() {



    @OptIn(ExperimentalCoroutinesApi::class)
    private val meta = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        metaRepository.observe(ac.normalizedInstanceDomain)
    }.flowOn(Dispatchers.IO)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val reactionCount = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        reactionHistoryDao.observeSumReactions(ac.normalizedInstanceDomain)
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userSetting = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        reactionUserSettingDao.observeByInstanceDomain(ac.normalizedInstanceDomain)
    }

    // 検索時の候補
    val uiState =
        combine(meta, accountStore.observeCurrentAccount, reactionCount, userSetting) { meta, ac, counts, settings ->
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
    val userSettingReactions: List<ReactionUserSetting>,
) {
    val frequencyUsedReactions: List<String> get() {
        return reactionHistoryCounts.map {
            it.reaction
        }.map { reaction ->
            if (reaction.codePointCount(0, reaction.length) == 1) {
                reaction
            } else if (reaction.startsWith(":") && reaction.endsWith(":") && reaction.contains(
                    "@"
                )
            ) {
                (reaction.replace(":", "").split("@")[0]).let {
                    ":$it:"
                }
            } else {
                reaction
            }
        }.filter { reaction ->
            reaction.codePointCount(0, reaction.length) == 1
                    || meta?.emojis?.any {
                it.name == reaction.replace(":", "")
            } ?: false
        }.distinct()
    }

    val frequencyUsedReactionsV2: List<EmojiType> by lazy {
        reactionHistoryCounts.map {
            it.reaction
        }.mapNotNull { reaction ->
            if (reaction.codePointCount(0, reaction.length) == 1) {
                EmojiType.UtfEmoji(reaction)
            } else if (reaction.startsWith(":") && reaction.endsWith(":") && reaction.contains(
                    "@"
                )) {
                (reaction.replace(":", "").split("@")[0]).let { name ->
                    meta?.emojis?.firstOrNull {
                        it.name == name
                    }?.let {
                        EmojiType.CustomEmoji(it)
                    }
                }
            } else {
                meta?.emojis?.firstOrNull {
                    it.name == reaction.replace(":", "")
                }?.let {
                    EmojiType.CustomEmoji(it)
                }
            }
        }.distinct()
    }

    val all: List<String> get() {
        return LegacyReaction.defaultReaction + (meta?.emojis?.map {
            ":${it.name}:"
        } ?: emptyList())
    }

    val allV2: List<EmojiType> get() {
        return LegacyReaction.defaultReaction.map {
            EmojiType.Legacy(it)
        } + (meta?.emojis?.map {
            EmojiType.CustomEmoji(it)
        } ?: emptyList())
    }

    val userSettingTextReactions: List<String> get() {
        return userSettingReactions.map {
            it.reaction
        }.ifEmpty {
            LegacyReaction.defaultReaction
        }
    }

    fun getCategoryBy(category: String): List<String> {
        return meta?.emojis?.filter {
            it.category == category

        }?.map {
            ":${it.name}:"
        }?: emptyList()
    }

    fun getCategoryByV2(category: String): List<EmojiType> {
        return meta?.emojis?.filter {
            it.category == category

        }?.map {
            EmojiType.CustomEmoji(it)
        }?: emptyList()
    }

}

sealed interface EmojiType {
    data class Legacy(val type: String) : EmojiType
    data class CustomEmoji(val emoji: Emoji) : EmojiType
    data class UtfEmoji(val code: String) : EmojiType
}

