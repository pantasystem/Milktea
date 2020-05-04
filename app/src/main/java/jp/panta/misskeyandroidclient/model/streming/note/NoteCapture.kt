package jp.panta.misskeyandroidclient.model.streming.note

import android.util.Log
import com.google.gson.JsonSyntaxException
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.streming.AbsObserver
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.lang.Exception
import java.util.*


class NoteCapture(val account: Account) : AbsObserver(){
    abstract class DeletedListener{
        val listenerId = UUID.randomUUID().toString()
        abstract fun onDeleted(noteId: String)
    }
    private val mNoteRegisters = WeakHashMap<String, NoteRegister>()

    private val mGson = GsonFactory.create()

    private val mNoteDeletedListenerMap= WeakHashMap<String, DeletedListener>()
    /**
     * ノートのキャプチャーを開始します。
     * @param registerId 担当するローカルデータ管理する対象のNoteRegister-Id
     * @param planeNoteViewData キャプチャーするNote
     * remote -> localの順番で登録されます
     * @return 正常に処理が完了した場合true
     */
    fun subscribe(registerId: String, planeNoteViewData: PlaneNoteViewData): Boolean{
        val noteRegister = synchronized(mNoteRegisters){
            mNoteRegisters[registerId]
        }

        if(noteRegister == null){
            Log.e("NoteCapture", "NoteRegister未登録です")
            return false
        }
        synchronized(noteRegister){
            val isRemoteCaptured = isRemoteCaptured(planeNoteViewData)

            noteRegister.add(planeNoteViewData)
            if(!isRemoteCaptured){
                // capture開始する
                capture(planeNoteViewData.toShowNote.id)
            }
        }
        return true

    }

    fun subscribeAll(registerId: String, notes: List<PlaneNoteViewData>){
        notes.forEach{
            subscribe(registerId, it)
        }
    }

    /**
     * ノートのキャプチャーを解消します。
     * 他に同じノートをキャプチャーしている場合remoteのキャプチャーは解除されません
     * ローカルのリソース解放アルゴリズムはNoteRegisterその次にNoteIdentityGroupに基づきます
     */
    fun unSubscribe(registerId: String, planeNoteViewData: PlaneNoteViewData): Boolean{
        val noteRegister = synchronized(mNoteRegisters){
            mNoteRegisters[registerId]
        }

        if(noteRegister == null){
            Log.d("NoteCapture", "NoteRegister未登録です")
            return false
        }

        synchronized(noteRegister){
            noteRegister.remove(planeNoteViewData)

            val isRemoteCaptured = isRemoteCaptured(planeNoteViewData)

            if(!isRemoteCaptured){
                // captureを解除する
                unCapture(planeNoteViewData.toShowNote.id)
            }
        }
        return true
    }

    fun unSubscribeAll(registerId: String, notes: List<PlaneNoteViewData>){
        notes.forEach{
            unSubscribe(registerId, it)
        }
    }

    /**
     * ノートのキャプチャーを一斉に開始します。
     * 他に同じノートをキャプチャーしている場合はremoteのキャプチャーは新たに開始されることはありません。
     * @param noteRegister 対象のRegisterのノートを一斉にremoteに登録します。すでに登録されている場合はremoteには登録されません。
     */
    fun attach(noteRegister: NoteRegister){
        val noteIds = noteRegister.registeredNoteIds()

        noteIds.forEach{ noteId ->
            val isRemoteCaptured = isRemoteCaptured(noteId)
            if(!isRemoteCaptured){
                capture(noteId)
            }
        }

        synchronized(mNoteRegisters){
            mNoteRegisters[noteRegister.registerId] = noteRegister
        }
    }

    /**
     * ノートのキャプチャーを一斉に解消します。
     * 他に同じノートをキャプチャーしている場合remoteのキャプチャーは解除されません。
     * またunSubscribeはローカルからもデータが削除されてしまうことがあるが、detachはRemoteから解除される
     * @param noteRegister 対象のRegisterのノートを一斉にremoteから解除します。他に登録されている場合はremoteには登録されません。
     */
    fun detach(noteRegister: NoteRegister){
        val noteIds = noteRegister.registeredNoteIds()
        synchronized(mNoteRegisters){
            mNoteRegisters.remove(noteRegister.registerId)
        }

        noteIds.forEach{ noteId ->
            val isRemoteCaptured = isRemoteCaptured(noteId)
            if(!isRemoteCaptured){
                unCapture(noteId)
            }
        }
    }

    fun addNoteDeletedListener(listener: DeletedListener){
        synchronized(mNoteDeletedListenerMap){
            mNoteDeletedListenerMap[listener.listenerId] = listener
        }
    }

    private fun capture(noteId: String){
        streamingAdapter?.send(mGson.toJson(createCaptureRequest(noteId)))
    }

    private fun unCapture(noteId: String){
        streamingAdapter?.send(mGson.toJson(createUnCaptureRequest(noteId)))
    }

    private fun createCaptureRequest(noteId: String): NoteCapture.CaptureRequest {
        return NoteCapture.CaptureRequest(body = NoteCapture.CaptureRequestBody(noteId))
    }

    private fun createUnCaptureRequest(noteId: String): NoteCapture.CaptureRequest {
        return NoteCapture.CaptureRequest(
            type = "unsubNote",
            body = NoteCapture.CaptureRequestBody(noteId)
        )
    }

    private fun isRemoteCaptured(planeNoteViewData: PlaneNoteViewData): Boolean{
        return synchronized(mNoteRegisters){
            mNoteRegisters.any{
                it.value.contains(planeNoteViewData)
            }
        }
    }

    private fun isRemoteCaptured(noteId: String): Boolean{
        synchronized(mNoteRegisters){
            return mNoteRegisters.any{
                it.value.contains(noteId)
            }
        }
    }

    override var streamingAdapter: StreamingAdapter? = null
    override fun onClosing() {

    }

    override fun onConnect() {
    }

    override fun onDisconnect() = Unit

    override fun onReceived(msg: String) {
        try{
            val receivedObject = mGson.fromJson(msg, NoteCapture.NoteUpdated::class.java)
            val id = receivedObject.body.id
            val userId = receivedObject.body.body?.userId
            val reaction = receivedObject.body.body?.reaction
            //val isRemoved = receivedObject.body.body?.deletedAt != null


            when (/*receivedObject.body.type == "deleted" -> noteRemovedListeners.forEach{
                            it.onRemoved(id)
                        }*/
                receivedObject.body.type) {
                "reacted" -> addReaction(id, reaction!!, receivedObject.body.body.emoji, userId)
                "unreacted" -> removeReaction(id, reaction!!, userId)
                "pollVoted" -> updatePoll(id, receivedObject.body.body?.choice!!, userId)
                "deleted" -> deleted(id)
                //else -> Log.d("NoteCapture", "不明なイベント")
            }

            //Log.d("NoteCapture", "onReceived: $receivedObject")
        }catch(e: JsonSyntaxException){
            //他のイベントが流れてくるので回避する
        }catch(e: Exception){

        }
    }

    private fun addReaction(noteId: String, reaction: String, emoji: Emoji?, actionUserId: String?){
       getNotes(noteId).forEach{
           it.addReaction(reaction, emoji, it.account.id == actionUserId)
       }
    }

    private fun removeReaction(noteId: String, reaction: String, actionUserId: String?){
        getNotes(noteId).forEach {
            it.takeReaction(reaction, it.account.id == actionUserId)
        }
    }

    private fun updatePoll(noteId: String, choice: Int, actionUserId: String?){
        getNotes(noteId).forEach{
            it.poll?.update(choice, account.id == actionUserId)
        }
    }

    private fun deleted(noteId: String){
        mNoteDeletedListenerMap.values.forEach{
            it.onDeleted(noteId)
        }
    }

    private fun getNotes(noteId: String): List<PlaneNoteViewData>{
        val updateTargets = ArrayList<PlaneNoteViewData>()
        synchronized(mNoteRegisters){
            val identityGroups = ArrayList<NoteIdentityGroup>()
            mNoteRegisters.values.forEach {
                synchronized(it){
                    identityGroups.addAll(
                        it.getNoteIdentityGroups().filter{ ng ->
                            ng.noteId == noteId
                        }
                    )
                }
            }
            identityGroups.forEach{
                synchronized(it){
                    updateTargets.addAll(it.getNotes())
                }
            }
        }
        return updateTargets
    }

}