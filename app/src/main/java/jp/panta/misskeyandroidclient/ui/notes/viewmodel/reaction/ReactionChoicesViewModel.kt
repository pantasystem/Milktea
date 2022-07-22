package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryCount
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSetting
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao
import javax.inject.Inject

@HiltViewModel
class ReactionChoicesViewModel @Inject constructor(
    accountStore: AccountStore,
    private val metaRepository: MetaRepository,
    private val reactionHistoryDao: ReactionHistoryDao,
    private val reactionUserSettingDao: ReactionUserSettingDao,
    loggerFactory: Logger.Factory,
) : ViewModel() {


    private val logger = loggerFactory.create("ReactionChoicesVM")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val meta = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        metaRepository.observe(ac.instanceDomain)
    }.flowOn(Dispatchers.IO)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val reactionCount = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        reactionHistoryDao.observeSumReactions(ac.instanceDomain)
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userSetting = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        reactionUserSettingDao.observeByInstanceDomain(ac.instanceDomain)
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
            SharingStarted.Lazily,
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

    val all: List<String> get() {
        return LegacyReaction.defaultReaction + (meta?.emojis?.map {
            ":${it.name}:"
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

}

