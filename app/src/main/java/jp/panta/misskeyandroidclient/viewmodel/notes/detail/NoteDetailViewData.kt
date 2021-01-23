package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLength
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(note: NoteDTO, account: Account, determineTextLength: DetermineTextLength) : PlaneNoteViewData(note, account, determineTextLength)