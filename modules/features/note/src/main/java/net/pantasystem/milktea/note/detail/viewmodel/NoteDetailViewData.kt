package net.pantasystem.milktea.note.detail.viewmodel

import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

/**
 * ノートの詳細表示となる一つのオブジェクト
 * 基本的にはPlaneNoteViewDataとは変わらないが
 * ListAdapterで詳細ビューの対象か判定する
 */
class NoteDetailViewData(
    note: NoteRelation,
    account: Account,
    translationStore: NoteTranslationStore,
    noteDataSource: NoteDataSource,
    configRepository: LocalConfigRepository,
    coroutineScope: CoroutineScope,
) : PlaneNoteViewData(
    note,
    account,
    translationStore,
    noteDataSource,
    configRepository,
    coroutineScope
) {
    init {
        super.reactionCountsExpanded.value = true
    }
}