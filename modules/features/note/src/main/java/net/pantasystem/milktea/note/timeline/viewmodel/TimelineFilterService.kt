package net.pantasystem.milktea.note.timeline.viewmodel

import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.filter.WordFilterService
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import javax.inject.Inject

class TimelineFilterService(
    private val pageable: Pageable,
    private val wordFilterService: WordFilterService,
) {

    class Factory @Inject constructor(
        private val wordFilterService: WordFilterService
    ){
        fun create(pageable: Pageable): TimelineFilterService {
            return TimelineFilterService(pageable, wordFilterService)
        }
    }

    suspend fun filterNotes(notes: List<PlaneNoteViewData>): List<PlaneNoteViewData> {
        return notes.mapNotNull { note ->
            val newResult = when(val result = note.filterResult) {
                PlaneNoteViewData.FilterResult.NotExecuted -> {
                    if (wordFilterService.isShouldFilterNote(pageable, note.note)) {
                        PlaneNoteViewData.FilterResult.ShouldFilterNote
                    } else {
                        PlaneNoteViewData.FilterResult.Pass
                    }
                }
                PlaneNoteViewData.FilterResult.ShouldFilterNote -> result
                PlaneNoteViewData.FilterResult.Pass -> result
            }
            note.filterResult = newResult
            if (newResult == PlaneNoteViewData.FilterResult.ShouldFilterNote) {
                null
            } else {
                note
            }
        }
    }
}