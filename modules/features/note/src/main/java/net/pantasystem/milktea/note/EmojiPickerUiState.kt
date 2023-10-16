package net.pantasystem.milktea.note

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.coroutines.combine
import net.pantasystem.milktea.common.text.LevenshteinDistance
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import net.pantasystem.milktea.model.note.reaction.LegacyReaction
import net.pantasystem.milktea.model.note.reaction.history.ReactionHistoryRepository


class EmojiPickerUiStateService(
    accountStore: AccountStore,
    private val customEmojiRepository: CustomEmojiRepository,
    private val reactionHistoryRepository: ReactionHistoryRepository,
    private val userEmojiConfigRepository: UserEmojiConfigRepository,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle,
    coroutineScope: CoroutineScope,
) {
    companion object {
        const val EXTRA_ACCOUNT_ID = "EmojiPickerViewModel.EXTRA_ACCOUNT_ID"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val account = savedStateHandle.getStateFlow<Long>(EXTRA_ACCOUNT_ID, -1L).map {
        it.takeIf {
            it > 0
        }
    }.flatMapLatest { specifiedId ->
        accountStore.getOrCurrent(specifiedId)
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val emojis = account
        .filterNotNull()
        .flatMapLatest { ac ->
            customEmojiRepository.observeBy(ac.getHost(), withAliases = true)
        }.catch {
            logger.error("絵文字の取得に失敗", it)
        }.flowOn(Dispatchers.IO)
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val reactionCount = account
        .filterNotNull()
        .flatMapLatest { ac ->
            reactionHistoryRepository.observeSumReactions(ac.normalizedInstanceUri)
        }.catch {
            logger.error("リアクション履歴の取得に失敗", it)
        }.flowOn(Dispatchers.IO)
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userSetting = account
        .filterNotNull()
        .flatMapLatest { ac ->
            userEmojiConfigRepository.observeByInstanceDomain(ac.normalizedInstanceUri)
        }.catch {
            logger.error("ユーザーリアクション設定情報の取得に失敗", it)
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recentlyUsedReactions =
        account.filterNotNull().flatMapLatest {
            reactionHistoryRepository.observeRecentlyUsedBy(it.normalizedInstanceUri, limit = 20)
        }.catch {
            logger.error("絵文字の直近使用履歴の取得に失敗", it)
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchWord = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredEmojis = searchWord.flatMapLatest { keyword ->
        account.filterNotNull().map { account ->
            keyword to account
        }
    }.flatMapLatest { (word, ac) ->
        customEmojiRepository.observeWithSearch(
            host = ac.getHost(),
            keyword = word.replace(":", "")
        ).map {
            it.sortedBy { emoji ->
                LevenshteinDistance(emoji.name, word)
            }
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val recentlyUsed = combine(
        recentlyUsedReactions,
        emojis,
        account
    ) { reactionHistoryCounts, _, account ->
        reactionHistoryCounts.map {
            it.reaction
        }.mapNotNull { reaction ->
            EmojiType.from(account?.getHost()?.let {
                customEmojiRepository.getAndConvertToMap(it)
            } ?: emptyMap(), reaction)
        }.distinct()
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val userSettingEmojis = combine(
        userSetting,
        emojis,
        account
    ) { userSettingReactions, _, account ->
        userSettingReactions.mapNotNull { setting ->
            EmojiType.from(account?.getHost()?.let {
                customEmojiRepository.getAndConvertToMap(it)
            } ?: emptyMap(), setting.reaction)
        }.ifEmpty {
            LegacyReaction.defaultReaction.map {
                EmojiType.Legacy(it)
            }
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val frequencyUsedReactionsV2 = combine(
        reactionCount,
        emojis,
        account,
    ) { reactionHistoryCounts, _, account ->
        reactionHistoryCounts.map {
            it.reaction
        }.mapNotNull { reaction ->
            EmojiType.from(account?.let {
                customEmojiRepository.getAndConvertToMap(it.getHost())
            } ?: emptyMap(), reaction)
        }.distinct()
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val otherEmojis = emojis.map { emojis ->
        emojis.filter {
            it.category.isNullOrBlank()
        }.map {
            EmojiType.CustomEmoji(it)
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val filteredEmojisViewData = filteredEmojis.map { emojis ->
        emojis.map {
            EmojiType.CustomEmoji(it)
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val categorisedEmojis = emojis.map { emojis ->
        emojis.filterNot {
            it.category.isNullOrBlank()
        }.groupBy {
            it.category ?: ""
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyMap()
    )

    val uiState = combine(
        searchWord,
        frequencyUsedReactionsV2,
        userSettingEmojis,
        recentlyUsed,
        otherEmojis,
        filteredEmojisViewData,
        categorisedEmojis
    ) { searchWord, frequencyUsedReactionsV2,
        userSettingEmojis,
        recentlyUsed,
        otherEmojis,
        filteredEmojisViewData,
        categorisedEmojis ->

        EmojiPickerUiState(
            keyword = searchWord,
            userSettingEmojis = userSettingEmojis,
            emojiListItems = generateEmojiListItems(
                keyword = searchWord,
                frequencyUsedReactionsV2 = frequencyUsedReactionsV2,
                userSettingEmojis = userSettingEmojis,
                otherEmojis = otherEmojis,
                recentlyUsed = recentlyUsed,
                filteredEmojis = filteredEmojisViewData,
                categorisedEmojis = categorisedEmojis
            )
        )
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        EmojiPickerUiState(
            keyword = "",
            userSettingEmojis = emptyList(),
            emojiListItems = emptyList()
        )
    )


}

data class EmojiPickerUiState(
    val keyword: String,
    val userSettingEmojis: List<EmojiType>,
    val emojiListItems: List<EmojiListItemType>,
) {

    val isSearchMode = keyword.isNotBlank()




    val tabHeaderLabels = emojiListItems.mapNotNull {
        (it as? EmojiListItemType.Header)?.label
    }

    fun isExistsConfig(emojiType: EmojiType): Boolean {
        return userSettingEmojis.any {
            emojiType.areItemsTheSame(it)
        }
    }


}


sealed interface EmojiListItemType {
    data class EmojiItem(val emoji: EmojiType) : EmojiListItemType

    data class Header(val label: StringSource) : EmojiListItemType
}

sealed interface EmojiType {
    data class Legacy(val type: String) : EmojiType
    data class CustomEmoji(val emoji: net.pantasystem.milktea.model.emoji.CustomEmoji) : EmojiType
    data class UtfEmoji(val code: String) : EmojiType
    companion object

    fun areItemsTheSame(other: EmojiType): Boolean {
        if (this === other) {
            return true
        }
        if (this.javaClass != other.javaClass) {
            return false
        }
        return when (this) {
            is CustomEmoji -> {
                emoji == (other as? CustomEmoji)?.emoji
            }

            is Legacy -> {
                type == (other as? Legacy)?.type
            }

            is UtfEmoji -> {
                code == (other as? UtfEmoji)?.code
            }
        }
    }

    fun areContentsTheSame(other: EmojiType): Boolean {
        return areItemsTheSame(other) && this == other
    }
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


fun EmojiType.Companion.from(emojis: Map<String, CustomEmoji>, reaction: String): EmojiType? {
    return if (reaction.codePointCount(0, reaction.length) == 1) {
        EmojiType.UtfEmoji(reaction)
    } else if (reaction.startsWith(":") && reaction.endsWith(":") && reaction.contains(
            "@"
        )
    ) {
        val nameOnly = reaction.replace(":", "")
        (emojis[nameOnly] ?: (nameOnly.split("@")[0]).let { name ->
            emojis[name]
        })?.let {
            EmojiType.CustomEmoji(it)
        }
    } else if (LegacyReaction.reactionMap[reaction] != null) {
        EmojiType.Legacy(reaction)
    } else {
        emojis[reaction.replace(":", "")]?.let {
            EmojiType.CustomEmoji(it)
        }
    }
}

private fun generateEmojiListItems(
    keyword: String,
    frequencyUsedReactionsV2: List<EmojiType>,
    userSettingEmojis: List<EmojiType>,
    recentlyUsed: List<EmojiType>,
    otherEmojis: List<EmojiType>,
    filteredEmojis: List<EmojiType>,
    categorisedEmojis: Map<String, List<CustomEmoji>>,
): List<EmojiListItemType> {
    return if (keyword.isBlank()) {
        listOf(
            EmojiListItemType.Header(StringSource.invoke(R.string.user)),
        ) + userSettingEmojis.map {
            EmojiListItemType.EmojiItem(it)
        } + EmojiListItemType.Header(StringSource.invoke(R.string.often_use)) + frequencyUsedReactionsV2.map {
            EmojiListItemType.EmojiItem(it)
        } + EmojiListItemType.Header(StringSource(R.string.recently_used)) + recentlyUsed.map {
            EmojiListItemType.EmojiItem(it)
        } + EmojiListItemType.Header(StringSource.invoke(R.string.other)) + otherEmojis.map {
            EmojiListItemType.EmojiItem(it)
        } + categorisedEmojis.map {
            listOf(EmojiListItemType.Header(StringSource.invoke(it.key))) + it.value.map { emoji ->
                EmojiListItemType.EmojiItem(EmojiType.CustomEmoji(emoji))
            }
        }.flatten()
    } else {
        filteredEmojis.sortedBy {
            LevenshteinDistance(it.toTextReaction(), keyword)
        }.map {
            EmojiListItemType.EmojiItem(it)
        }
    }
}
