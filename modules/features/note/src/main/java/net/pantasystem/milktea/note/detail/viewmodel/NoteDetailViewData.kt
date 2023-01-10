package net.pantasystem.milktea.note.detail.viewmodel

import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(
    note: NoteRelation,
    account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    translationStore: NoteTranslationStore,
    instanceEmojis: List<Emoji>,
) : PlaneNoteViewData(note, account, noteCaptureAPIAdapter, translationStore, instanceEmojis)