package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteTranslationStore

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(
    note: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: NoteTranslationStore
) : PlaneNoteViewData(note, account, noteCaptureAPIAdapter, translationStore)