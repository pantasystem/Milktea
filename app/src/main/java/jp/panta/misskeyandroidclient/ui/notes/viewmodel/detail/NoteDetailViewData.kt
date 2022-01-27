package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(note: NoteRelation, account: Account, noteCaptureAPIAdapter: NoteCaptureAPIAdapter, translationStore: NoteTranslationStore) : PlaneNoteViewData(note, account, noteCaptureAPIAdapter, translationStore)