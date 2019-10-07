package jp.panta.misskeyandroidclient.model.notes

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.util.Log
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*
import java.util.Collections.addAll
import kotlin.collections.ArrayList

class LiveNotePagingStore(private val i: String?, private val timeline:(TimelineRequest) -> Call<List<Note>?>, private val callBack: CallBack) {

    private var mLiveData: MediatorLiveData<List<PlaneNoteViewData>> = MediatorLiveData()

    var mSinceId: String? = null
    var mUntilId: String? = null

    /**
     * 読み込む、書き込む前にVersionをローカルとして保持
     * 読み込み直後にローカルのVersionと比較し等価であれば更新をする
     */
    private val mVersion = Version()

    private var isLoading: Boolean = false

    private class Version{
        var version: Int = 0
            private set
        fun incrementVersion(){
            synchronized(version){
                version ++
            }
        }

    }

    /**
     * LiveDataに更新があったときにこのコールバックの実装クラスが呼ばれます
     * SwipeRefreshなどを初期化するときに使います。
     */
    interface CallBack{

        /**
         * onLoad()は更新があったときに呼び出される
         * 更新があったときに呼び出される
         * Exceptionが発せした時はonError()が呼び出される。
         * 戻り値が何もなかった場合はonLoad()が呼び出される
         */
        fun onLoad()
        fun onError(t: Throwable)

    }

    fun loadInit(): LiveData<List<PlaneNoteViewData>?> {
        isLoading = true

        val version = mVersion.version



        GlobalScope.launch{
            try{

                val tmp = timeline(
                    TimelineRequest(i = i, limit = 30)
                ).execute()
                val list = tmp.body()
                val code = tmp.code()


                Log.d("LivePaging", "ok $code, ${tmp}")
                //versionをチェックする
                if(version != mVersion.version){
                    //return@launch mLiveData.postValue(list)
                    callBack.onLoad()
                    return@launch
                }

                Log.d("LivePaging", "$list")

                if(list!=null){
                    mLiveData.postValue(toViewData(list))
                }
                mVersion.incrementVersion()

                //sinceId, untilIdを更新する必要がある
                mSinceId = list?.firstOrNull()?.id
                mUntilId = list?.lastOrNull()?.id

            }catch(e: Exception){
                callBack.onError(e)
            }

        }
        isLoading = false
        return mLiveData
    }

    fun loadNew(){
        if(isLoading){
            return
        }
        isLoading = true

        val sinceId = mSinceId

        sinceId?: throw IllegalArgumentException("loadNewを呼び出すにはloadInitを呼び出す必要があります")

        val version = mVersion.version

        timeline(TimelineRequest(i = i, sinceId = sinceId)).enqueue(object  : Callback<List<Note>?>{
            override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {

                val list = response.body()?.asReversed()
                if(version != mVersion.version){
                    callBack.onLoad()
                    isLoading = false
                    return
                }

                if( list == null ){
                    callBack.onLoad()
                    isLoading = false
                    return
                }

                if(mLiveData.value == null){
                    mLiveData.postValue(toViewData(list))
                }else{
                    val newList =  ArrayList<PlaneNoteViewData>(mLiveData.value!!).apply{
                        addAll(0, toViewData(list))
                    }
                    mLiveData.postValue(newList)
                }
                mVersion.incrementVersion()

                //sinceId, untilIdを更新する
                if(mUntilId == null){
                    mUntilId = list.lastOrNull()?.id
                }

                val newSinceId = list.firstOrNull()?.id
                if(newSinceId!= null){
                    mSinceId = newSinceId
                }

                callBack.onLoad()
                isLoading = false
            }
            override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                callBack.onError(t)
                isLoading = false
            }
        })

    }

    fun loadOld(){
        if(isLoading){
            return
        }
        isLoading = true

        val untilId = mUntilId

        untilId?: throw IllegalArgumentException("loadNewを呼び出すにはloadInitを呼び出す必要があります")

        Log.d("", "untilId: $untilId")

        val version = mVersion.version

        timeline(TimelineRequest(i = i, untilId = untilId))
            .enqueue(object : Callback<List<Note>?>{
                override fun onResponse(call: Call<List<Note>?>, response: Response<List<Note>?>) {
                    if(version != mVersion.version){
                        callBack.onLoad()
                        isLoading = false
                        return
                    }

                    val list = response.body()

                    if( list == null ){
                        callBack.onLoad()
                        isLoading = false
                        return
                    }

                    if(mLiveData.value == null){
                        mLiveData.postValue(toViewData(list))
                    }else{
                        val newList =  ArrayList<PlaneNoteViewData>(mLiveData.value!!).apply{
                            addAll(toViewData(list))
                        }
                        mLiveData.postValue(newList)
                    }

                    mVersion.incrementVersion()

                    //sinceId, untilIdを更新する
                    if(mSinceId == null){
                        mSinceId = list.firstOrNull()?.id
                    }


                    val newUntilId = list.lastOrNull()?.id
                    if(newUntilId != null){
                        mUntilId = newUntilId
                    }

                    callBack.onLoad()
                    isLoading = false
                }

                override fun onFailure(call: Call<List<Note>?>, t: Throwable) {
                    callBack.onError(t)
                }
            })


    }

    fun addNew(note: Note){
        if(isLoading){
            return
        }
        mVersion.incrementVersion()
        val list = mLiveData.value
        val newList = if(list == null){
            toViewData(arrayListOf(note))
        }else{

            ArrayList<PlaneNoteViewData>(list).apply{
                add(0, PlaneNoteViewData(note))
            }
        }
        mSinceId = note.id

        mLiveData.postValue(newList)

        //ArrayList<Note>()
    }

    fun addOld(note: Note){
        mVersion.incrementVersion()
        val list = mLiveData.value
        val newList = if(list == null){
            toViewData(arrayListOf(note))
        }else{
            ArrayList<PlaneNoteViewData>(list).apply{
                add(PlaneNoteViewData(note))
            }
        }
        mUntilId = note.id

        mLiveData.postValue(newList)
    }

    fun addAllNew(notes: List<Note>){
        if(notes.isEmpty())
            return

        mVersion.incrementVersion()
        val list = mLiveData.value

        val newList = if(list == null){
            toViewData(notes)
        }else{
            ArrayList<PlaneNoteViewData>(list).apply{
                addAll(0, toViewData(notes))
            }
        }

        mSinceId = notes.first().id

        mLiveData.postValue(newList)
    }

    fun addAllOld(notes: List<Note>){
        if(notes.isEmpty())
            return

        mVersion.incrementVersion()
        val list = mLiveData.value

        val newList = if(list == null){
            toViewData(notes)
        }else{
            ArrayList<PlaneNoteViewData>(list).apply{
                addAll(toViewData(notes))
            }
        }

        mUntilId = notes.last().id


    }

    fun setLiveData(liveData: MediatorLiveData<List<PlaneNoteViewData>>){
        this.mLiveData = liveData
    }

    private fun toViewData(list: List<Note>): List<PlaneNoteViewData>{
        return list.map{
            PlaneNoteViewData(it)
        }
    }

}