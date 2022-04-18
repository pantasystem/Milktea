package jp.panta.misskeyandroidclient.ui.notes.viewmodel.favorite

import net.pantasystem.milktea.data.model.fevorite.Favorite
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

class FavoriteNoteViewData(
    private val favorite: Favorite,
    noteRelation: net.pantasystem.milktea.model.notes.NoteRelation,
    account: net.pantasystem.milktea.model.account.Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: net.pantasystem.milktea.model.notes.NoteTranslationStore
) : PlaneNoteViewData(noteRelation, account,noteCaptureAPIAdapter, translationStore){
    override fun getRequestId(): String {
        return favorite.id
    }
}