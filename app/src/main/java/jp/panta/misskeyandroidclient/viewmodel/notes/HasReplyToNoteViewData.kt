package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import java.lang.IllegalArgumentException

class HasReplyToNoteViewData(note: Note, account: Account, determineTextLength: DetermineTextLength)  : PlaneNoteViewData(note, account, determineTextLength){
    val reply = note.reply

    /*val replyToAvatarUrl = reply?.user?.avatarUrl
    val replyToName = reply?.user?.name
    val replyToUserName = reply?.user?.userName
    val replyToText = reply?.text

    val replyToCw = reply?.cw
    //true　折り畳み
    val replyToContentFolding = MutableLiveData<Boolean>( replyToCw != null )
    val replyToContentFoldingStatusMessage = Transformations.map(replyToContentFolding){
        if(it) "もっと見る: ${subNoteText?.length}" else "閉じる"
    }
    fun changeReplyToContentFolding(){

    }*/
    val replyTo = if(reply == null){
        throw IllegalArgumentException("replyがnullですPlaneNoteViewDataを利用してください")
    }else{
        PlaneNoteViewData(reply, account, determineTextLength.clone())
    }




}