package jp.panta.misskeyandroidclient.viewmodel.setting.page

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PageSettingViewModel(
    val miCore: MiCore,
    val settingStore: SettingStore
) : ViewModel(){
    val encryption = miCore.getEncryption()

    val selectedPages = MediatorLiveData<List<Page>>()

    var accountRelation = miCore.currentAccount.value

    init{
        selectedPages.addSource(miCore.currentAccount){
            accountRelation = it
            selectedPages.value = it.pages
        }
    }

    fun setList(pages: List<Page>){
        selectedPages.value = pages.sortedBy{
            it.pageNumber
        }
    }
    fun save(){
        val list = selectedPages.value?: emptyList()
        list.forEachIndexed { index, page ->
            page.pageNumber = index + 1
        }
        miCore.replaceAllPagesInCurrentAccount(list)
    }

    fun addPage(page: Page){
        val list = ArrayList<Page>(selectedPages.value?: emptyList())
        list.add(page)
        setList(list)
    }

    fun syncAddUser(userId: String){
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

}