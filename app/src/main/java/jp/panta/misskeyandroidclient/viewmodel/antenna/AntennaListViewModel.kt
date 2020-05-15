package jp.panta.misskeyandroidclient.viewmodel.antenna

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
){

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

    init{
        antennas.addSource(miCore.currentAccount){
            initLoad()
        }
    }


    fun initLoad(){
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
            }

            override fun onFailure(call: Call<List<Antenna>>, t: Throwable) {
                Log.e(TAG, "アンテナ一覧の取得に失敗しました。", t)
            }
        })
    }

    fun toggleTab(antenna: Antenna?){
        antenna?: return
        miCore.addPageInCurrentAccount(PageableTemplate.antenna(antenna))
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