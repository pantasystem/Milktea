package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import java.lang.IllegalArgumentException

class HasReplyToNoteViewData(
    noteRelation: NoteRelation,
    account: Account,
    determineTextLength: DetermineTextLength,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    noteTranslationStore: NoteTranslationStore,
)  : PlaneNoteViewData(noteRelation, account, determineTextLength, noteCaptureAPIAdapter, noteTranslationStore){
    val reply = noteRelation.reply

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
        PlaneNoteViewData(reply, account, determineTextLength.clone(), noteCaptureAPIAdapter, noteTranslationStore)
    }




}