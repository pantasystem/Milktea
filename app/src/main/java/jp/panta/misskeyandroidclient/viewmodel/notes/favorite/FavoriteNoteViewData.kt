package jp.panta.misskeyandroidclient.viewmodel.notes.favorite

import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLength
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class FavoriteNoteViewData(
    private val favorite: Favorite,
    account: Account,
    determineTextLength: DetermineTextLength
) : PlaneNoteViewData(favorite.note, account, determineTextLength){
    override fun getRequestId(): String {
        return favorite.id
    }
}