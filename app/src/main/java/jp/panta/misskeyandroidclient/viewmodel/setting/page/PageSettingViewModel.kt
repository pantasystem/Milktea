package jp.panta.misskeyandroidclient.viewmodel.setting.page

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
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

    class Factory(val miApplication: MiApplication) : ViewModelProvider.Factory{

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PageSettingViewModel(miApplication, miApplication.settingStore) as T
        }
    }
    val encryption = miCore.getEncryption()

    val selectedPages = MediatorLiveData<List<Page>>()

    var accountRelation = miCore.currentAccount.value

    var defaultPages = MutableLiveData<List<Page>>()

    init{
        selectedPages.addSource(miCore.currentAccount){
            accountRelation = it
            selectedPages.value = if(it.pages.isEmpty()){
                defaultPages.value
            }else{
                it.pages
            }
        }
        selectedPages.addSource(defaultPages){
            val ex = selectedPages.value?: emptyList()
            if(ex.isEmpty()){
                selectedPages.value = ex
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