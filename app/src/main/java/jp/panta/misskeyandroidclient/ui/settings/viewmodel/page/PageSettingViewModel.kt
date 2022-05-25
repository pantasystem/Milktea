package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.ui.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.*
import net.pantasystem.milktea.model.user.User

class PageSettingViewModel(
    val miCore: MiCore,
    val settingStore: SettingStore,
    private val pageTypeNameMap: PageTypeNameMap
) : ViewModel(), SelectPageTypeToAdd, PageSettingAction {

    class Factory(val miApplication: MiApplication) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PageSettingViewModel(
                miApplication,
                miApplication.getSettingStore(),
                PageTypeNameMap(miApplication)
            ) as T
        }
    }

    val encryption = miCore.getEncryption()

    val selectedPages = MediatorLiveData<List<Page>>()

    var account: Account? = null
        get() = miCore.getAccountStore().currentAccount
        private set


    val pageAddedEvent = EventBus<PageType>()

    val pageOnActionEvent = EventBus<Page>()

    val pageOnUpdateEvent = EventBus<Page>()

    init {

        miCore.getAccountStore().observeCurrentAccount.filterNotNull().onEach {
            account = it
            selectedPages.postValue(
                it.pages.sortedBy { p ->
                    p.weight
                }
            )
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun setList(pages: List<Page>) {
        selectedPages.value = pages.mapIndexed { index, page ->
            page.apply {
                weight = index + 1
            }
        }
    }

    fun save() {
        val list = selectedPages.value ?: emptyList()
        list.forEachIndexed { index, page ->
            page.weight = index + 1
        }
        Log.d("PageSettingVM", "pages:$list")
        viewModelScope.launch(Dispatchers.IO) {
            miCore.getAccountStore().replaceAllPage(list).onFailure {
                Log.e("PageSettingVM", "保存失敗", it)
            }
        }
    }

    fun updatePage(page: Page) {
        val pages = selectedPages.value?.let {
            ArrayList(it)
        } ?: return

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
        val list = ArrayList<Page>(selectedPages.value ?: emptyList())
        page.weight = list.size
        list.add(page)
        setList(list)
    }

    fun addUserPageById(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                miCore.getMisskeyAPIProvider().get(account!!).showUser(
                    RequestUser(userId = userId, i = account?.getI(encryption))
                ).throwIfHasError().body() ?: throw IllegalStateException()
            }.onSuccess {
                addUserPage(it)
            }.onFailure { t ->
                Log.e("PageSettingVM", "ユーザーの取得に失敗した", t)
            }

        }
    }

    private fun addUserPage(user: UserDTO) {
        val page = if (settingStore.isUserNameDefault) {
            PageableTemplate(account!!)
                .user(user.id, title = user.shortDisplayName)
        } else {
            PageableTemplate(account!!)
                .user(user.id, title = user.displayName)
        }
        addPage(page)
    }

    fun addUsersGalleryById(userId: String) {
        val pageable = Pageable.Gallery.User(userId = userId)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = miCore.getUserRepository()
                    .find(User.Id(accountId = account!!.accountId, id = userId))
                val name =
                    if (settingStore.isUserNameDefault) user.shortDisplayName else user.displayName
                val page = account!!.newPage(pageable, name = name)
                addPage(page)
            }
        }
    }

    fun removePage(page: Page) {
        val list = ArrayList<Page>(selectedPages.value ?: emptyList())
        list.remove(page)
        setList(list)
    }


    override fun add(type: PageType) {
        pageAddedEvent.event = type
        val name = pageTypeNameMap.get(type)
        when (type) {
            PageType.GLOBAL -> {
                addPage(PageableTemplate(account!!).globalTimeline(name))
            }
            PageType.SOCIAL -> {
                addPage(PageableTemplate(account!!).hybridTimeline(name))
            }
            PageType.LOCAL -> {
                addPage(PageableTemplate(account!!).localTimeline(name))
            }
            PageType.HOME -> {
                addPage(PageableTemplate(account!!).homeTimeline(name))
            }
            PageType.NOTIFICATION -> {
                addPage(PageableTemplate(account!!).notification(name))
            }
            PageType.FAVORITE -> {
                addPage(PageableTemplate(account!!).favorite(name))
            }
            PageType.FEATURED -> {
                addPage(PageableTemplate(account!!).featured(name))
            }
            PageType.MENTION -> {
                addPage(PageableTemplate(account!!).mention(name))
            }
            PageType.GALLERY_FEATURED -> addPage(
                account!!.newPage(
                    Pageable.Gallery.Featured, name
                )
            )
            PageType.GALLERY_POPULAR -> addPage(
                account!!.newPage(
                    Pageable.Gallery.Popular, name
                )
            )
            PageType.GALLERY_POSTS -> addPage(
                account!!.newPage(
                    Pageable.Gallery.Posts, name
                )
            )
            PageType.MY_GALLERY_POSTS -> addPage(
                account!!.newPage(
                    Pageable.Gallery.MyPosts, name
                )
            )
            PageType.I_LIKED_GALLERY_POSTS -> addPage(
                account!!.newPage(
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