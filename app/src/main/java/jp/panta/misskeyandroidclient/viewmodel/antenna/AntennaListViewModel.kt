package jp.panta.misskeyandroidclient.viewmodel.antenna

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.antenna.Antenna
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class AntennaListViewModel (
    val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AntennaListViewModel(miCore) as T
        }
    }

    companion object{
        const val TAG = "AntennaViewModel"
    }

    val antennas = MediatorLiveData<List<Antenna>>()

    val editAntennaEvent = EventBus<Antenna>()

    val confirmDeletionAntennaEvent = EventBus<Antenna>()

    private val openAntennasTimelineEvent = EventBus<Antenna>()

    val isLoading = MutableLiveData(false)
    private var mIsLoading: Boolean = false
        set(value) {
            field = value
            isLoading.postValue(value)
        }

    private val mPagedAntennaIds = MutableLiveData<Set<Antenna.Id>>()
    val pagedAntennaIds: LiveData<Set<Antenna.Id>> = mPagedAntennaIds

    var account: Account? = null

    init{

        miCore.getCurrentAccount().onEach {
            if(account?.accountId != it?.accountId) {
                loadInit()
                account = it
            }
            mPagedAntennaIds.postValue(
                it?.pages?.mapNotNull { page ->
                    val pageable = page.pageable()
                    if (pageable is Pageable.Antenna) {
                        account?.accountId?.let { accountId ->
                            Antenna.Id(accountId, pageable.antennaId)

                        }
                    } else {
                        null
                    }
                }?.toSet()?: emptySet()
            )
        }.launchIn(viewModelScope + Dispatchers.IO)


    }

    val deleteResultEvent = EventBus<Boolean>()

    fun loadInit(){
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().getCurrentAccount()
                val res = (miCore.getMisskeyAPIProvider().get(account) as MisskeyAPIV12).getAntennas(
                    AntennaQuery(
                        i = account.getI(miCore.getEncryption()),
                        limit = null,
                        antennaId = null
                    )
                ).body()
                    ?: throw IllegalStateException("アンテナの取得に失敗しました")
                res.map { dto ->
                    dto.toEntity(account)
                }
            }.onSuccess {
                antennas.postValue(it)
            }
            mIsLoading = false
        }

    }

    fun toggleTab(antenna: Antenna?){
        antenna?: return
        val paged = account?.pages?.firstOrNull {

            it.pageParams.antennaId == antenna.id.antennaId
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
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val i = account?.getI(miCore.getEncryption())
                    ?: return@launch
                getMisskeyAPI()?.deleteAntenna(AntennaQuery(
                    i = i,
                    antennaId = antenna.id.antennaId,
                    limit = null
                ))?.throwIfHasError()
            }.onSuccess {
                loadInit()
            }.onFailure {
                deleteResultEvent.event = false

            }

        }


    }

    private fun getMisskeyAPI(): MisskeyAPIV12?{
        return miCore.getMisskeyAPIProvider().get(miCore.getCurrentAccount().value!!) as? MisskeyAPIV12
    }
}