package jp.panta.misskeyandroidclient.ui.notes.viewmodel.favorite

import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.fevorite.Favorite
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.data.model.notes.NoteRelation
import net.pantasystem.milktea.data.model.notes.NoteTranslationStore
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