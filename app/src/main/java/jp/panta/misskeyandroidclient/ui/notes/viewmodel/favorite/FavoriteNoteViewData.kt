package jp.panta.misskeyandroidclient.ui.notes.viewmodel.favorite

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

class FavoriteNoteViewData(
    private val favorite: Favorite,
    noteRelation: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: NoteTranslationStore
) : PlaneNoteViewData(noteRelation, account,noteCaptureAPIAdapter, translationStore){
    override fun getRequestId(): String {
        return favorite.id
    }
}