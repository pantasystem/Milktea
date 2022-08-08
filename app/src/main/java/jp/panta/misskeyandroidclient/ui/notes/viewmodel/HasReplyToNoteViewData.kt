package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore

class HasReplyToNoteViewData(
    noteRelation: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    noteTranslationStore: NoteTranslationStore,
)  : PlaneNoteViewData(noteRelation, account, noteCaptureAPIAdapter, noteTranslationStore){
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
        PlaneNoteViewData(reply, account, noteCaptureAPIAdapter, noteTranslationStore)
    }




}