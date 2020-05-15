package jp.panta.misskeyandroidclient.viewmodel.antenna

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.list.ListId
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.model.v12.antenna.AntennaToAdd
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.util.regex.Pattern

/**
 * @param antenna 新規作成時はnullになる
 */
class AntennaEditorViewModel (
    val accountRelation: AccountRelation,
    val miCore: MiCore,
    antenna: Antenna?
) : ViewModel(){

    val antenna = MutableLiveData<Antenna?>(antenna)

    enum class Source(val remote: String){
        HOME("home"), ALL("all"), USERS("users"), LIST("list"), GROUP("group")
    }

    val name = Transformations.map(this.antenna){
        it?.name?: ""
    }
    val source = Transformations.map(this.antenna){ a ->
        Source.values().firstOrNull {
            a?.src ==  it.remote
        }?: Source.ALL
    }
    val keywords = Transformations.map(this.antenna){
        setupKeywords(it?.keywords)
    }

    val excludeKeywords = Transformations.map(this.antenna){
        setupKeywords(it?.excludeKeywords)
    }

    val users = MediatorLiveData<List<UserViewData>>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.users?.map{ userId ->
                UserViewData(userId)
            }?: emptyList()
        }

        addSource(this){
            val ci = accountRelation.getCurrentConnectionInformation()
                ?: return@addSource
            val i = ci.getI(miCore.getEncryption())
                ?: return@addSource
            it.forEach { uvd ->
                uvd.setApi(i, miCore.getMisskeyAPI(ci))
            }
        }
    }

    val userList = MediatorLiveData<UserList?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            val ci = accountRelation.getCurrentConnectionInformation()
                ?: return@addSource
            val i = ci.getI(miCore.getEncryption())
                ?: return@addSource
            it?.userListId?.let{ userListId ->
                miCore.getMisskeyAPI(ci).showList(ListId(i, userListId)).enqueue(object : Callback<UserList>{
                    override fun onResponse(call: Call<UserList>, response: Response<UserList>) {
                        this@apply.postValue(response.body())
                    }

                    override fun onFailure(call: Call<UserList>, t: Throwable) {
                        Log.e("AntennaEditorViewModel", "userListの読み込みに失敗しました", t)
                    }
                })
            }
        }
    }

    /**
     * 新しいノートを通知します
     */
    val notify = Transformations.map(this.antenna){
        it?.notify?: false
    }

    /**
     * ファイルが添付されたノートのみ
     */
    val withFile = Transformations.map(this.antenna){
        it?.withFile?: false
    }

    /**
     * 大文字と小文字を区別します
     */
    val caseSensitive = Transformations.map(this.antenna){
        it?.caseSensitive?: false
    }

    /**
     * 返信を含める
     */
    val withReplies = Transformations.map(this.antenna){
        it?.withReplies?: false
    }
    
    fun addRemote(){
        val ci = accountRelation.getCurrentConnectionInformation()
            ?: return
        val i = ci.getI(miCore.getEncryption())
            ?: return
        val antenna = this.antenna.value
        val api = miCore.getMisskeyAPI(ci) as? MisskeyAPIV12
            ?: return
        val antennaAPI = if(antenna == null){
            api::createAntenna
        }else{
            api::updateAntenna
        }
        val request = AntennaToAdd(
            i,
            antenna?.id,
            name.value?: antenna?.name?: "",
            source.value?.remote!!,
            userList.value?.id,
            null,
            toListKeywords(keywords.value?: ""),
            toListKeywords(excludeKeywords.value?: ""),
            users.value?.map{
                it.userId
            }?: emptyList(),
            caseSensitive.value?: false,
            withFile.value?: false,
            withReplies.value?: false,
            notify.value?: false

        )
        antennaAPI.invoke(request).enqueue(object : Callback<Antenna>{
            override fun onResponse(call: Call<Antenna>, response: Response<Antenna>) {
                this@AntennaEditorViewModel.antenna.postValue(response.body())
            }

            override fun onFailure(call: Call<Antenna>, t: Throwable) {
                Log.e("AntennaEditorViewModel", "add antenna error", t)
            }
        })
    }
    
    private fun setupKeywords(keywords: List<List<String>>?): String{
        val builder = StringBuilder()
        keywords?.forEach {  list ->
            list.forEach {  word ->
                builder.append(word)
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    private fun toListKeywords(keywords: String): List<List<String>>{
        return keywords.split('\n').map{
            it.split(Pattern.compile("""[ 　]"""))
        }
    }

}