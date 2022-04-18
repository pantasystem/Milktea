package jp.panta.misskeyandroidclient.ui.notes.viewmodel.favorite

import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

class FavoriteNoteViewData(
    private val favorite: net.pantasystem.milktea.api.misskey.favorite.Favorite,
    noteRelation: net.pantasystem.milktea.model.notes.NoteRelation,
    account: net.pantasystem.milktea.model.account.Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: net.pantasystem.milktea.model.notes.NoteTranslationStore
) : PlaneNoteViewData(noteRelation, account,noteCaptureAPIAdapter, translationStore){
    override fun getRequestId(): String {
        return favorite.id
    }
}