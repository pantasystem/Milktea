package net.pantasystem.milktea.note.reaction

import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

sealed interface ReactionCountAction {
    data class OnClicked(val note: PlaneNoteViewData, val reaction: String) : ReactionCountAction
    data class OnLongClicked(val note: PlaneNoteViewData, val reaction: String) :
        ReactionCountAction
}