package jp.panta.misskeyandroidclient.viewmodel.antenna

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.model.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AntennaListViewModel (
    val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AntennaListViewModel(miCore) as T
        }
    }

    companion object{
        const val TAG = "AntennaViewModel"
    }

    val antennas = MediatorLiveData<List<Antenna>>()

    val editAntennaEvent = EventBus<Antenna>()

    val confirmDeletionAntennaEvent = EventBus<Antenna>()

    val openAntennasTimelineEvent = EventBus<Antenna>()

    val isLoading = MutableLiveData<Boolean>(false)

    val pagedAntennaIds = Transformations.map(miCore.currentAccount){
        it.pages.mapNotNull { page ->
            val pageable = page.pageable()
            if (pageable is Page.Antenna) {
                pageable.antennaId
            } else {
                null
            }
        }.toSet()
    }

    var account: AccountRelation? = null

    init{
        antennas.addSource(miCore.currentAccount){
            if(account?.account != it?.account){
                loadInit()
                account = it
            }
        }
    }


    fun loadInit(){
        isLoading.value = true
        val i = miCore.currentAccount.value?.getCurrentConnectionInformation()?.getI(miCore.getEncryption())
            ?: return
        getMisskeyAPI()?.getAntennas(
            AntennaQuery(
                i = i,
                limit = null,
                antennaId = null
            )
        )?.enqueue(object : Callback<List<Antenna>>{
            override fun onResponse(call: Call<List<Antenna>>, response: Response<List<Antenna>>) {
                antennas.postValue(response.body())
                isLoading.postValue(false)
            }

            override fun onFailure(call: Call<List<Antenna>>, t: Throwable) {
                Log.e(TAG, "アンテナ一覧の取得に失敗しました。", t)
                isLoading.postValue(false)
            }
        })
    }

    fun toggleTab(antenna: Antenna?){
        antenna?: return
        val paged = account?.pages?.firstOrNull {
            it.antenna?.antennaId == antenna.id
        }
        if(paged == null){
            miCore.addPageInCurrentAccount(PageableTemplate.antenna(antenna))
        }else{
            miCore.removePageInCurrentAccount(paged)
        }
    }

    fun confirmDeletionAntenna(antenna: Antenna?){
        antenna?: return
        confirmDeletionAntennaEvent.event = antenna
    }

    fun editAntenna(antenna: Antenna?){
        antenna?: return
        editAntennaEvent.event = antenna
    }

    fun openAntennasTimeline(antenna: Antenna?){
        openAntennasTimelineEvent.event = antenna
    }

    private fun getMisskeyAPI(): MisskeyAPIV12?{
        return miCore.getMisskeyAPI(miCore.currentAccount.value) as? MisskeyAPIV12
    }
}