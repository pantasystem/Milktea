package jp.panta.misskeyandroidclient.viewmodel.notes

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableArrayList
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelineViewModel(val requestBaseSetting: NoteRequest.Setting) : ViewModel(){


    val observableTimelineList: ObservableArrayList<PlaneNoteViewData> = ObservableArrayList()

    val isLoading = MediatorLiveData<Boolean>()

    val errorState = MediatorLiveData<String>()

    private val baseUrl = "https://misskey.io/"

    private val misskeyAPI = MisskeyAPIServiceBuilder.build(baseUrl)

    private val timelineStore = when(requestBaseSetting.type){
        NoteType.HOME -> misskeyAPI::homeTimeline
        NoteType.LOCAL -> misskeyAPI::localTimeline
        NoteType.SOCIAL -> misskeyAPI::hybridTimeline
        NoteType.GLOBAL -> misskeyAPI::globalTimeline
        NoteType.SEARCH -> misskeyAPI::searchNote
        NoteType.SEARCH_HASH -> misskeyAPI::searchByTag
        NoteType.USER -> misskeyAPI::userNotes

    }

    private var isLoadingFlag = false

    fun loadNew(){
        this.isLoading.postValue(true)
        if( ! isLoadingFlag ){
            isLoadingFlag = true
            val sinceId = observableTimelineList.firstOrNull()?.id
            if(sinceId == null){
                isLoadingFlag = false
                //初期化処理 or 何もしない
            }else{

                val req = requestBaseSetting.buildRequest(NoteRequest.Conditions(sinceId = sinceId))
                timelineStore(req).enqueue(object : Callback<List<Note>?>{
                    override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                        val newNotes = response.body()?.asReversed()
                        isLoadingFlag = false
                        val planeNotes = newNotes?.map{ it -> PlaneNoteViewData(it) }
                            ?: return


                        observableTimelineList.addAll(0, planeNotes)
                        isLoading.postValue(false)

                    }

                    override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                        isLoadingFlag = false
                        isLoading.postValue(false)
                    }
                })
            }

        }
    }

    fun loadOld(){
        val untilId = observableTimelineList.lastOrNull()?.id
        if(  isLoadingFlag || untilId == null){
            //何もしない
        }else{
            isLoadingFlag = true
            val req = requestBaseSetting.buildRequest(NoteRequest.Conditions(untilId = untilId))
            timelineStore(req).enqueue(object : Callback<List<Note>?>{
                override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                    val list = response.body()?.map{ it -> PlaneNoteViewData(it) }

                    if(list == null){
                        isLoadingFlag = false
                        return
                    }

                    observableTimelineList.addAll(list)
                    isLoadingFlag = false

                }
                override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                    isLoadingFlag = false
                }
            })

        }
    }

    fun loadInit(){
        this.isLoading.postValue(true)

        if( ! isLoadingFlag ){

            isLoadingFlag = true

            timelineStore(requestBaseSetting.buildRequest(NoteRequest.Conditions())).enqueue( object : Callback<List<Note>?>{
                override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                    val list = response.body()?.map{ it -> PlaneNoteViewData(it) }
                    if(list == null){
                        isLoadingFlag = false
                        return
                    }

                    observableTimelineList.clear()
                    observableTimelineList.addAll(list)
                    isLoadingFlag = false

                    //test()
                }

                override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                    isLoadingFlag = false
                }
            })
        }
    }

    private fun test(){
        val first = observableTimelineList.firstOrNull()
        GlobalScope.launch{
            for(n in 0.until(100)){
                //first?.replyCount = n.toString()
                first?.addReaction("")

                delay(100)
            }
        }
    }


}