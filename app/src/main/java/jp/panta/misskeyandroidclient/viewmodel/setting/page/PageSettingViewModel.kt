package jp.panta.misskeyandroidclient.viewmodel.setting.page

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.PageType
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalStateException

class PageSettingViewModel(
    val miCore: MiCore,
    val settingStore: SettingStore,
    val pageTypeNameMap: PageTypeNameMap
) : ViewModel(), SelectPageTypeToAdd, PageSettingAction{

    class Factory(val miApplication: MiApplication) : ViewModelProvider.Factory{

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PageSettingViewModel(miApplication, miApplication.getSettingStore(), PageTypeNameMap(miApplication)) as T
        }
    }
    val encryption = miCore.getEncryption()

    val selectedPages = MediatorLiveData<List<Page>>()

    var account = miCore.getCurrentAccount().value


    val pageAddedEvent = EventBus<PageType>()

    val pageOnActionEvent = EventBus<Page>()

    val pageOnUpdateEvent = EventBus<Page>()

    init{

        miCore.getCurrentAccount().filterNotNull().onEach {
            account = it
            selectedPages.postValue(
                it.pages.sortedBy { p ->
                    p.weight
                }
            )
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun setList(pages: List<Page>){
        selectedPages.value = pages.mapIndexed { index, page ->
            page.apply{
                weight = index + 1
            }
        }
    }
    fun save(){
        val list = selectedPages.value?: emptyList()
        list.forEachIndexed { index, page ->
            page.weight = index + 1
        }
        Log.d("PageSettingVM", "pages:$list")
        miCore.replaceAllPagesInCurrentAccount(list)
    }

    fun updatePage(page: Page){
        val pages = selectedPages.value?.let{
            ArrayList(it)
        } ?: return

        var pageIndex = pages.indexOfFirst {
            it.pageId == page.pageId && it.pageId > 0
        }
        if(pageIndex < 0){
            pageIndex = pages.indexOfFirst{
                it.weight == page.weight && page.weight > 0
            }
        }
        if(pageIndex >= 0 && pageIndex < pages.size){
            pages[pageIndex] = page.copy()
        }

        setList(pages)

    }
    fun addPage(page: Page){
        val list = ArrayList<Page>(selectedPages.value?: emptyList())
        page.weight = list.size
        list.add(page)
        setList(list)
    }

    fun addUserPageById(userId: String){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                miCore.getMisskeyAPI(account!!).showUser(
                    RequestUser(userId = userId, i = account?.getI(encryption))
                ).throwIfHasError().body()?: throw IllegalStateException()
            }.onSuccess {
                addUserPage(it)
            }.onFailure { t ->
                Log.e("PageSettingVM", "ユーザーの取得に失敗した", t)
            }

        }
    }
    fun addUserPage(user: UserDTO){
        val page = if(settingStore.isUserNameDefault){
            PageableTemplate(account!!).user(user.id, title = user.getShortDisplayName())
        }else{
            PageableTemplate(account!!).user(user.id, title = user.getDisplayName())
        }
        addPage(page)
    }

    fun addUsersGalleryById(userId: String) {
        val pageable = Pageable.Gallery.User(userId = userId)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = miCore.getUserRepository().find(User.Id(accountId = account!!.accountId, id = userId))
                val name = if(settingStore.isUserNameDefault) user.getShortDisplayName() else user.getDisplayName()
                val page =  account!!.newPage(pageable, name = name)
                addPage(page)
            }
        }
    }

    fun removePage(page: Page){
        val list = ArrayList<Page>(selectedPages.value?: emptyList())
        list.remove(page)
        setList(list)
    }



    override fun add(type: PageType) {
        pageAddedEvent.event = type
        val name = pageTypeNameMap.get(type)
        when(type){
            PageType.GLOBAL->{
                addPage(PageableTemplate(account!!).globalTimeline(name))
            }
            PageType.SOCIAL->{
                addPage(PageableTemplate(account!!).hybridTimeline(name))
            }
            PageType.LOCAL -> {
                addPage(PageableTemplate(account!!).localTimeline(name))
            }
            PageType.HOME ->{
                addPage(PageableTemplate(account!!).homeTimeline(name))
            }
            PageType.NOTIFICATION ->{
                addPage(PageableTemplate(account!!).notification(name))
            }
            PageType.FAVORITE ->{
                addPage(PageableTemplate(account!!).favorite(name))
            }
            PageType.FEATURED ->{
                addPage(PageableTemplate(account!!).featured(name))
            }
            PageType.MENTION ->{
                addPage(PageableTemplate(account!!).mention(name))
            }
            PageType.GALLERY_FEATURED -> addPage(account!!.newPage(Pageable.Gallery.Featured, name))
            PageType.GALLERY_POPULAR -> addPage(account!!.newPage(Pageable.Gallery.Popular, name))
            PageType.GALLERY_POSTS -> addPage(account!!.newPage(Pageable.Gallery.Posts, name))
            PageType.MY_GALLERY_POSTS -> addPage(account!!.newPage(Pageable.Gallery.MyPosts, name))
            PageType.I_LIKED_GALLERY_POSTS -> addPage(account!!.newPage(Pageable.Gallery.ILikedPosts, name))
            else -> {
                Log.d("PageSettingViewModel", "管轄外な設定パターン:$type, name:$name")
            }
        }
    }

    override fun action(page: Page?) {
        page?: return
        pageOnActionEvent.event = page
    }

    private fun writeTheNumberOfPages(index: Int, page: Page){
        page.weight = index + 1
    }
}