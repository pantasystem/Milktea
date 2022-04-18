package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(note: net.pantasystem.milktea.model.notes.NoteRelation, account: net.pantasystem.milktea.model.account.Account, noteCaptureAPIAdapter: NoteCaptureAPIAdapter, translationStore: net.pantasystem.milktea.model.notes.NoteTranslationStore) : PlaneNoteViewData(note, account, noteCaptureAPIAdapter, translationStore)