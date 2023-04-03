package net.pantasystem.milktea.setting.viewmodel.page

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.*
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
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
    private val nodeInfoRepository: NodeInfoRepository,
) : ViewModel(), SelectPageTypeToAdd, PageSettingAction {

    val selectedPages = MutableStateFlow<List<Page>>(emptyList())

    val account = accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pageAddedEvent = EventBus<PageType>()

    val pageOnActionEvent = EventBus<Page>()

    val pageOnUpdateEvent = EventBus<Page>()

    val pageTypes = account.filterNotNull().map {
        val nodeInfo = nodeInfoRepository.find(it.getHost()).getOrNull()
        val version = nodeInfo?.type?.getVersion() ?: Version("0")
        val isCalckey = nodeInfo?.type is NodeInfo.SoftwareType.Misskey.Calckey
        when(it.instanceType) {
            Account.InstanceType.MISSKEY -> {
                listOfNotNull(
                    PageType.HOME,
                    PageType.LOCAL,
                    PageType.SOCIAL,
                    PageType.GLOBAL,
                    if (isCalckey) PageType.CALCKEY_RECOMMENDED_TIMELINE else null,
                    if (version >= Version("12")) PageType.ANTENNA else null,
                    PageType.NOTIFICATION,
                    PageType.USER_LIST,
                    PageType.MENTION,
                    PageType.FAVORITE,
                    if (version >= Version("12")) PageType.CHANNEL_TIMELINE else null,
                    if (version >= Version("12")) PageType.CLIP_NOTES else null,
                    PageType.SEARCH,
                    PageType.SEARCH_HASH,
                    PageType.USER,
                    PageType.FEATURED,
                    PageType.DETAIL,
                ) + if (version >= Version("12.75.0")) {
                    listOf(
                        PageType.GALLERY_FEATURED,
                        PageType.GALLERY_POPULAR,
                        PageType.GALLERY_POSTS,
                        PageType.USERS_GALLERY_POSTS,
                        PageType.MY_GALLERY_POSTS,
                        PageType.I_LIKED_GALLERY_POSTS,
                    )
                } else {
                    emptyList()
                }
            }
            Account.InstanceType.MASTODON -> {
                listOf(
                    PageType.MASTODON_HOME_TIMELINE,
                    PageType.MASTODON_LOCAL_TIMELINE,
                    PageType.MASTODON_PUBLIC_TIMELINE,
//                    PageType.MASTODON_HASHTAG_TIMELINE,
                    PageType.NOTIFICATION,
                    PageType.FAVORITE,
                    PageType.MASTODON_LIST_TIMELINE,
                    PageType.MASTODON_BOOKMARK_TIMELINE,
//                    PageType.MASTODON_USER_TIMELINE,

                )
            }
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

    private fun addPage(page: Page) {
        val list = ArrayList<Page>(selectedPages.value)
        page.weight = list.size
        list.add(page)
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
        addPage(page)
    }

    fun addUsersGalleryByIds(userIds: List<User.Id>) {
        viewModelScope.launch  {
            runCancellableCatching {
                userIds.map {
                    async {
                        userRepository.find(it)
                    }
                }.awaitAll().map { user ->
                    val name =
                        if (settingStore.isUserNameDefault) user.shortDisplayName else user.displayName
                    account.value!!.newPage(Pageable.Gallery.User(userId = user.id.id), name = name)
                }.forEach(::addPage)
            }
        }
    }

    fun removePage(page: Page) {
        val list = ArrayList<Page>(selectedPages.value)
        list.remove(page)
        setList(list)
    }


    override fun add(type: PageType) {
        pageAddedEvent.event = type
        val name = pageTypeNameMap.get(type)
        when (type) {
            PageType.GLOBAL -> {
                addPage(PageableTemplate(account.value!!).globalTimeline(name))
            }
            PageType.SOCIAL -> {
                addPage(PageableTemplate(account.value!!).hybridTimeline(name))
            }
            PageType.LOCAL -> {
                addPage(PageableTemplate(account.value!!).localTimeline(name))
            }
            PageType.HOME -> {
                addPage(PageableTemplate(account.value!!).homeTimeline(name))
            }
            PageType.NOTIFICATION -> {
                addPage(PageableTemplate(account.value!!).notification(name))
            }
            PageType.FAVORITE -> {
                addPage(PageableTemplate(account.value!!).favorite(name))
            }
            PageType.FEATURED -> {
                addPage(PageableTemplate(account.value!!).featured(name))
            }
            PageType.MENTION -> {
                addPage(PageableTemplate(account.value!!).mention(name))
            }
            PageType.GALLERY_FEATURED -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.Featured, name
                )
            )
            PageType.GALLERY_POPULAR -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.Popular, name
                )
            )
            PageType.GALLERY_POSTS -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.Posts, name
                )
            )
            PageType.MY_GALLERY_POSTS -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.MyPosts, name
                )
            )
            PageType.I_LIKED_GALLERY_POSTS -> addPage(
                account.value!!.newPage(
                    Pageable.Gallery.ILikedPosts,
                    name
                )
            )
            PageType.MASTODON_HOME_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.HomeTimeline,
                    name,
                )
            )
            PageType.MASTODON_LOCAL_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.LocalTimeline(),
                    name,
                )
            )
            PageType.MASTODON_PUBLIC_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.PublicTimeline(),
                    name,
                )
            )
            PageType.CALCKEY_RECOMMENDED_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.CalckeyRecommendedTimeline,
                    name,
                )
            )
            PageType.MASTODON_BOOKMARK_TIMELINE -> addPage(
                account.value!!.newPage(
                    Pageable.Mastodon.BookmarkTimeline,
                    name,
                )
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