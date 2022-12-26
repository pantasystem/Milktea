package net.pantasystem.milktea.setting.viewmodel.page

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
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
    private val misskeyAPIProvider: MisskeyAPIProvider,
) : ViewModel(), SelectPageTypeToAdd, PageSettingAction {

    val selectedPages = MutableStateFlow<List<Page>>(emptyList())

    val account = accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pageAddedEvent = EventBus<PageType>()

    val pageOnActionEvent = EventBus<Page>()

    val pageOnUpdateEvent = EventBus<Page>()

    val pageTypes = account.filterNotNull().map {
        var pageTypeList = PageType.values().toList().toMutableList()
        val api = misskeyAPIProvider.get(accountStore.state.value.currentAccount!!)
        if(api !is MisskeyAPIV12){
            pageTypeList.remove(PageType.ANTENNA)
        }
        if(api !is MisskeyAPIV1275) {
            pageTypeList = pageTypeList.filterNot {
                galleryTypes.contains(it)
            }.toMutableList()
        }
        pageTypeList
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