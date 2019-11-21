package jp.panta.misskeyandroidclient.viewmodel.notes.favorite

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class FavoriteNoteViewData(
    private val favorite: Favorite,
    connectionInstance: ConnectionInstance
) : PlaneNoteViewData(favorite.note, connectionInstance){
    override fun getRequestId(): String {
        return favorite.id
    }
}