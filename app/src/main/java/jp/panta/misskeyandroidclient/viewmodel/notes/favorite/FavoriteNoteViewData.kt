package jp.panta.misskeyandroidclient.viewmodel.notes.favorite

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLength
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class FavoriteNoteViewData(
    private val favorite: Favorite,
    noteRelation: NoteRelation,
    account: Account,
    determineTextLength: DetermineTextLength,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher
) : PlaneNoteViewData(noteRelation, account, determineTextLength, noteCaptureAPIAdapter, coroutineScope, coroutineDispatcher){
    override fun getRequestId(): String {
        return favorite.id
    }
}