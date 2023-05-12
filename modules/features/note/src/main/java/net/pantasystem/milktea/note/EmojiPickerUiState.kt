package net.pantasystem.milktea.note

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.text.LevenshteinDistance
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.UserEmojiConfig
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryCount
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository


class EmojiPickerUiStateService(
    accountStore: AccountStore,
    private val customEmojiRepository: CustomEmojiRepository,
    private val reactionHistoryRepository: ReactionHistoryRepository,
    private val userEmojiConfigRepository: UserEmojiConfigRepository,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
) {


    @OptIn(ExperimentalCoroutinesApi::class)
    private val emojis = accountStore.observeCurrentAccount
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { ac ->
            customEmojiRepository.observeBy(ac.getHost())
        }.catch {
            logger.error("絵文字の取得に失敗", it)
        }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val reactionCount = accountStore
        .observeCurrentAccount
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { ac ->
            reactionHistoryRepository.observeSumReactions(ac.normalizedInstanceUri)
                .distinctUntilChanged()
        }.catch {
            logger.error("リアクション履歴の取得に失敗", it)
        }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userSetting = accountStore.observeCurrentAccount
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { ac ->
            userEmojiConfigRepository.observeByInstanceDomain(ac.normalizedInstanceUri)
                .distinctUntilChanged()
        }.catch {
            logger.error("ユーザーリアクション設定情報の取得に失敗", it)
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val reactions = combine(reactionCount, userSetting) { counts, settings ->
        Reactions(
            settings,
            counts,
        )
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        Reactions(emptyList(), emptyList())
    )

    private val baseInfo = combine(accountStore.observeCurrentAccount, emojis) { account, emojis ->
        BaseInfo(account, emojis)
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        BaseInfo(null, emptyList())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recentlyUsedReactions =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            reactionHistoryRepository.observeRecentlyUsedBy(it.normalizedInstanceUri, limit = 20)
        }.catch {
            logger.error("絵文字の直近使用履歴の取得に失敗", it)
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchWord = MutableStateFlow("")


    // 検索時の候補
    val uiState: StateFlow<EmojiPickerUiState> = combine(
        searchWord,
        baseInfo,
        reactions,
        recentlyUsedReactions
    ) { word, (ac, emojis), (settings, counts), recentlyUsed ->
        EmojiPickerUiState(
            keyword = word,
            account = ac,
            customEmojis = emojis,
            reactionHistoryCounts = counts,
            userSettingReactions = settings,
            recentlyUsedReactions = recentlyUsed
        )
    }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        EmojiPickerUiState(
            "",
            null, emptyList(),
            emptyList(),
            emptyList(),
            emptyList()
        )
    )

}

data class EmojiPickerUiState(
    val keyword: String,
    val account: Account?,
    val customEmojis: List<Emoji>,
    val reactionHistoryCounts: List<ReactionHistoryCount>,
    val userSettingReactions: List<UserEmojiConfig>,
    val recentlyUsedReactions: List<ReactionHistory>,
) {

    val isSearchMode = keyword.isNotBlank()

    private val frequencyUsedReactionsV2: List<EmojiType> by lazy {
        reactionHistoryCounts.map {
            it.reaction
        }.mapNotNull { reaction ->
            EmojiType.from(customEmojis, reaction)
        }.distinct()
    }

    private val userSettingEmojis: List<EmojiType> by lazy {
        userSettingReactions.mapNotNull { setting ->
            EmojiType.from(customEmojis, setting.reaction)
        }.ifEmpty {
            LegacyReaction.defaultReaction.map {
                EmojiType.Legacy(it)
            }
        }
    }

    private val otherEmojis = customEmojis.filter {
        it.category.isNullOrBlank()
    }.map {
        EmojiType.CustomEmoji(it)
    }

    private fun getCategoryBy(category: String): List<EmojiType> {
        return customEmojis.filter {
            it.category == category

        }.map {
            EmojiType.CustomEmoji(it)
        }
    }

    private val categories = customEmojis.filterNot {
        it.category.isNullOrBlank()
    }.mapNotNull {
        it.category
    }.distinct()

    private val recentlyUsed = recentlyUsedReactions.mapNotNull {
        EmojiType.from(customEmojis, it.reaction)
    }


    val emojiListItems: List<EmojiListItemType> = listOf(
        EmojiListItemType.Header(StringSource.invoke(R.string.user)),
    ) + userSettingEmojis.map {
        EmojiListItemType.EmojiItem(it)
    } + EmojiListItemType.Header(StringSource.invoke(R.string.often_use)) + frequencyUsedReactionsV2.map {
        EmojiListItemType.EmojiItem(it)
    } + EmojiListItemType.Header(StringSource(R.string.recently_used)) + recentlyUsed.map {
        EmojiListItemType.EmojiItem(it)
    } + EmojiListItemType.Header(StringSource.invoke(R.string.other)) + otherEmojis.map {
        EmojiListItemType.EmojiItem(it)
    } + categories.map { category ->
        listOf(EmojiListItemType.Header(StringSource.invoke(category))) + getCategoryBy(category).map {
            EmojiListItemType.EmojiItem(it)
        }
    }.flatten()

    val searchFilteredEmojis = customEmojis.filterEmojiBy(keyword).map {
        EmojiType.CustomEmoji(it)
    }.sortedBy {
        LevenshteinDistance(it.emoji.name, keyword)
    }

    val tabLabels = emojiListItems.mapNotNull {
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
    data class CustomEmoji(val emoji: Emoji) : EmojiType
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

fun List<Emoji>.filterEmojiBy(word: String): List<Emoji> {
    val w = word.replace(":", "")
    return filter { emoji ->
        emoji.name.contains(w)
                || emoji.aliases?.any { alias ->
            alias.startsWith(w)
        } ?: false
    }
}

private data class Reactions(
    val userSettings: List<UserEmojiConfig>,
    val reactionHistoryCounts: List<ReactionHistoryCount>,
)

private data class BaseInfo(
    val account: Account?,
    val emojis: List<Emoji>,
)