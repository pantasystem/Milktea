package jp.panta.misskeyandroidclient.viewmodel.notes.favorite

import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class FavoriteNoteViewData(private val favorite: Favorite) : PlaneNoteViewData(favorite.note){
    override fun getRequestId(): String {
        return favorite.id
    }
}