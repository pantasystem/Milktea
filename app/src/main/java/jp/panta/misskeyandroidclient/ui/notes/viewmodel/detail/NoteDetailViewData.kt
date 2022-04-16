package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.data.model.notes.NoteRelation
import net.pantasystem.milktea.data.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(note: NoteRelation, account: Account, noteCaptureAPIAdapter: NoteCaptureAPIAdapter, translationStore: NoteTranslationStore) : PlaneNoteViewData(note, account, noteCaptureAPIAdapter, translationStore)