package jp.panta.misskeyandroidclient.viewmodel.antenna

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
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

    val pagedAntennaIds = Transformations.map(miCore.getCurrentAccount()){
        it.pages.mapNotNull { page ->
            val pageable = page.pageable()
            if (pageable is Pageable.Antenna) {
                pageable.antennaId
            } else {
                null
            }
        }.toSet()
    }

    var account: Account? = null

    init{
        antennas.addSource(miCore.getCurrentAccount()){
            if(account?.accountId != it?.accountId){
                loadInit()
                account = it
            }
        }
    }

    val deleteResultEvent = EventBus<Boolean>()

    fun loadInit(){
        isLoading.value = true
        val i = miCore.getCurrentAccount().value?.getI(miCore.getEncryption())
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

            it.pageParams.antennaId == antenna.id
        }
        if(paged == null){
            miCore.addPageInCurrentAccount(PageableTemplate(account!!).antenna(antenna))
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

    fun deleteAntenna(antenna: Antenna){
        account?.getI(miCore.getEncryption())?.let{ i ->
            getMisskeyAPI()?.deleteAntenna(AntennaQuery(
                i = i,
                antennaId = antenna.id,
                limit = null
            ))?.enqueue(object : Callback<Unit>{
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    deleteResultEvent.event = response.code() in 200 until 300
                    if(response.code() in 200 until 300){
                        loadInit()
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    deleteResultEvent.event = false
                }
            })
        }

    }

    private fun getMisskeyAPI(): MisskeyAPIV12?{
        return miCore.getMisskeyAPI(miCore.getCurrentAccount().value!!) as? MisskeyAPIV12
    }
}