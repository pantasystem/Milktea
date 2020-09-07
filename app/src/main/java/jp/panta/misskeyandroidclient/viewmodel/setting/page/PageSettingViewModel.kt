package jp.panta.misskeyandroidclient.viewmodel.setting.page

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.PageType
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    var accountRelation = miCore.currentAccount.value


    val pageAddedEvent = EventBus<PageType>()

    val pageOnActionEvent = EventBus<Page>()

    val pageOnUpdateEvent = EventBus<Page>()

    init{
        selectedPages.addSource(miCore.currentAccount){
            accountRelation = it
            selectedPages.value = it.pages.sortedBy { p ->
                p.pageNumber
            }
        }

    }

    fun setList(pages: List<Page>){
        selectedPages.value = pages.mapIndexed { index, page ->
            page.apply{
                pageNumber = index + 1
            }
        }
    }
    fun save(){
        val list = selectedPages.value?: emptyList()
        list.forEachIndexed { index, page ->
            page.pageNumber = index + 1
        }
        Log.d("PageSettingVM", "pages:$list")
        miCore.replaceAllPagesInCurrentAccount(list)
    }

    fun updatePage(page: Page){
        val pages = selectedPages.value?.let{
            ArrayList(it)
        } ?: return

        var pageIndex = pages.indexOfFirst {
            it.id == page.id && it.id != null
        }
        if(pageIndex < 0){
            pageIndex = pages.indexOfFirst{
                it.pageNumber == page.pageNumber && page.pageNumber != null
            }
        }
        if(pageIndex >= 0 && pageIndex < pages.size){
            pages[pageIndex] = page.copy()
        }

        setList(pages)

    }
    fun addPage(page: Page){
        val list = ArrayList<Page>(selectedPages.value?: emptyList())
        page.pageNumber = list.size
        list.add(page)
        setList(list)
    }

    fun addUserPageById(userId: String){
        miCore.getMisskeyAPI(accountRelation)?.showUser(
            RequestUser(userId = userId, i = accountRelation?.getCurrentConnectionInformation()?.getI(encryption))
        )?.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                if(user != null){
                    addUserPage(user)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("PageSettingVM", "ユーザーの取得に失敗した", t)
            }
        })
    }
    fun addUserPage(user: User){
        val page = if(settingStore.isUserNameDefault){
            PageableTemplate.user(user.id, title = user.getShortDisplayName())
        }else{
            PageableTemplate.user(user.id, title = user.getDisplayName())
        }
        addPage(page)
    }

    fun removePage(page: Page){
        val list = ArrayList<Page>(selectedPages.value?: emptyList())
        list.remove(page)
        setList(list)
    }

    fun asyncAddUser(userId: String){
        miCore.getMisskeyAPI(accountRelation)?.showUser(
            RequestUser(
                i = accountRelation?.getCurrentConnectionInformation()?.getI(encryption)!!,
                userId = userId
            )
        )?.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.code() in 200.until(300)){
                    addPage(PageableTemplate.user(response.body()!!, settingStore.isUserNameDefault))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
            }
        })
    }


    override fun add(type: PageType) {
        pageAddedEvent.event = type
        val name = pageTypeNameMap.get(type)
        when(type){
            PageType.GLOBAL->{
                addPage(PageableTemplate.globalTimeline(name))
            }
            PageType.SOCIAL->{
                addPage(PageableTemplate.hybridTimeline(name))
            }
            PageType.LOCAL -> {
                addPage(PageableTemplate.localTimeline(name))
            }
            PageType.HOME ->{
                addPage(PageableTemplate.homeTimeline(name))
            }
            PageType.NOTIFICATION ->{
                addPage(PageableTemplate.notification(name))
            }
            PageType.FAVORITE ->{
                addPage(PageableTemplate.favorite(name))
            }
            PageType.FEATURED ->{
                addPage(PageableTemplate.featured(name))
            }
            PageType.MENTION ->{
                addPage(PageableTemplate.mention(name))
            }
            else -> {

            }
        }
    }

    override fun action(page: Page?) {
        page?: return
        pageOnActionEvent.event = page
    }

    private fun writeTheNumberOfPages(index: Int, page: Page){
        page.pageNumber = index + 1
    }
}