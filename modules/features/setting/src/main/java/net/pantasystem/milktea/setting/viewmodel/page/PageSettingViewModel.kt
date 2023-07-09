package net.pantasystem.milktea.setting.viewmodel.page

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.*
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.setting.PageTypeNameMap
import javax.inject.Inject

@HiltViewModel
class PageSettingViewModel @Inject constructor(
    val settingStore: SettingStore,
    private val pageTypeNameMap: PageTypeNameMap,
    private val userRepository: UserRepository,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val pageCandidateGenerator: PageCandidateGenerator,
) : ViewModel(), SelectPageTypeToAdd, PageSettingAction {

    val selectedPages = MutableStateFlow<List<Page>>(emptyList())

    val account =
        accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pageAddedEvent = EventBus<PageCandidate>()

    val pageOnActionEvent = EventBus<Page>()

    val pageOnUpdateEvent = EventBus<Page>()

    val pageTypesGroupedByAccount = combine(
        accountStore.observeCurrentAccount,
        accountStore.observeAccounts,
    ) { ca, accounts ->
        (listOfNotNull(
            ca
        ) + accounts.filterNot {
            it.accountId == ca?.accountId
        }).map {
            PageCandidateGroup(
                ca,
                it,
                pageCandidateGenerator.createPageCandidates(it, ca)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            account.collect {
                selectedPages.value = it?.pages ?: emptyList()
            }
        }
    }

    fun setList(pages: List<Page>) {
        selectedPages.value = pages.mapIndexed { index, page ->
            page.apply {
                weight = index + 1
            }
        }
    }

    fun save() {
        val list = selectedPages.value
        list.forEachIndexed { index, page ->
            page.weight = index + 1
        }
        Log.d("PageSettingVM", "pages:$list")
        viewModelScope.launch {
            accountStore.replaceAllPage(list).onFailure {
                Log.e("PageSettingVM", "保存失敗", it)
            }
        }
    }

    fun updatePage(page: Page) {
        val pages = ArrayList(selectedPages.value)

        var pageIndex = pages.indexOfFirst {
            it.pageId == page.pageId && it.pageId > 0
        }
        if (pageIndex < 0) {
            pageIndex = pages.indexOfFirst {
                it.weight == page.weight && page.weight > 0
            }
        }
        if (pageIndex >= 0 && pageIndex < pages.size) {
            pages[pageIndex] = page.copy()
        }

        setList(pages)

    }

    private fun addPage(page: Page, relatedAccount: Account?) {
        val p = if (relatedAccount == null || page.accountId == relatedAccount.accountId) {
            page
        } else {
            page.copy(
                attachedAccountId = relatedAccount.accountId,
                title = page.title + ("(@${relatedAccount.userName}@${relatedAccount.getHost()})")
            )
        }
        val list = ArrayList<Page>(selectedPages.value)
        p.weight = list.size
        list.add(p)
        setList(list)
    }

    fun addUserPageByIds(userIds: List<User.Id>) {
        viewModelScope.launch {
            runCancellableCatching {
                userIds.map {
                    async {
                        userRepository.find(it)
                    }
                }.awaitAll()
            }.onSuccess { list ->
                list.map(::addUserPage)
            }.onFailure { t ->
                Log.e("PageSettingVM", "ユーザーの取得に失敗した", t)
            }

        }
    }

    private fun addUserPage(user: User) {
        val page = if (settingStore.isUserNameDefault) {
            PageableTemplate(account.value!!)
                .user(user.id.id, title = user.shortDisplayName)
        } else {
            PageableTemplate(account.value!!)
                .user(user.id.id, title = user.displayName)
        }
        addPage(page, null)
    }

    fun addUsersGalleryByIds(userIds: List<User.Id>) {
        viewModelScope.launch {
            runCancellableCatching {
                val account = requireNotNull(account.value)
                userIds.map {
                    async {
                        userRepository.find(it)
                    }
                }.awaitAll().map { user ->
                    val relatedAccount = accountRepository.get(user.id.accountId).getOrThrow()
                    val name = if (settingStore.isUserNameDefault) user.shortDisplayName else user.displayName
                    val title = if (relatedAccount.accountId == account.accountId) {
                        name
                    } else {
                        "$name(${relatedAccount.getAcct()})"
                    }
                    account.newPage(Pageable.Gallery.User(userId = user.id.id), name = title) to relatedAccount
                }.forEach { (page, relatedAccount) ->
                    addPage(page, relatedAccount)
                }
            }
        }
    }

    fun removePage(page: Page) {
        val list = ArrayList<Page>(selectedPages.value)
        list.remove(page)
        setList(list)
    }


    override fun add(type: PageCandidate) {
        pageAddedEvent.event = type
        val name = pageTypeNameMap.get(type.type)
        when (type.type) {
            PageType.GLOBAL -> {
                addPage(PageableTemplate(account.value!!).globalTimeline(name), type.relatedAccount)
            }
            PageType.SOCIAL -> {
                addPage(PageableTemplate(account.value!!).hybridTimeline(name), type.relatedAccount)
            }
            PageType.LOCAL -> {
                addPage(PageableTemplate(account.value!!).localTimeline(name), type.relatedAccount)
            }
            PageType.HOME -> {
                addPage(PageableTemplate(account.value!!).homeTimeline(name), type.relatedAccount)
            }
            PageType.NOTIFICATION -> {
                addPage(PageableTemplate(account.value!!).notification(name), type.relatedAccount)
            }
            PageType.FAVORITE -> {
                addPage(PageableTemplate(account.value!!).favorite(name), type.relatedAccount)
            }
            PageType.FEATURED -> {
                addPage(PageableTemplate(account.value!!).featured(name), type.relatedAccount)
            }
            PageType.MENTION -> {
                addPage(PageableTemplate(account.value!!).mention(name), type.relatedAccount)
            }
            PageType.GALLERY_FEATURED -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.Featured, name
                ),
                type.relatedAccount
            )
            PageType.GALLERY_POPULAR -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.Popular, name
                ),
                type.relatedAccount
            )
            PageType.GALLERY_POSTS -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.Posts, name
                ),
                type.relatedAccount
            )
            PageType.MY_GALLERY_POSTS -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.MyPosts, name
                ),
                type.relatedAccount
            )
            PageType.I_LIKED_GALLERY_POSTS -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.ILikedPosts,
                    name
                ),
                type.relatedAccount
            )
            PageType.MASTODON_HOME_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.HomeTimeline,
                    name,
                ),
                type.relatedAccount
            )
            PageType.MASTODON_LOCAL_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.LocalTimeline(),
                    name,
                ),
                type.relatedAccount
            )
            PageType.MASTODON_PUBLIC_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.PublicTimeline(),
                    name,
                ),
                type.relatedAccount
            )
            PageType.CALCKEY_RECOMMENDED_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.CalckeyRecommendedTimeline,
                    name,
                ),
                type.relatedAccount
            )
            PageType.MASTODON_BOOKMARK_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.BookmarkTimeline,
                    name,
                ),
                type.relatedAccount
            )
            else -> {
                Log.d("PageSettingViewModel", "管轄外な設定パターン:$type, name:$name")
            }
        }
    }

    override fun action(page: Page?) {
        page ?: return
        pageOnActionEvent.event = page
    }

}

data class PageCandidate(
    val relatedAccount: Account,
    val type: PageType,
    val name: StringSource,
)

data class PageCandidateGroup(
    val currentAccount: Account?,
    val relatedAccount: Account,
    val candidates: List<PageCandidate>,
)