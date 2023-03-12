package net.pantasystem.milktea.note.timeline.viewmodel

import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.filter.WordFilterService
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import javax.inject.Inject

class TimelineFilterService(
    private val pageable: Pageable,
    private val wordFilterService: WordFilterService,
    private val renoteMuteRepository: RenoteMuteRepository,
) {

    class Factory @Inject constructor(
        private val wordFilterService: WordFilterService,
        private val renoteMuteRepository: RenoteMuteRepository,
    ){
        fun create(pageable: Pageable): TimelineFilterService {
            return TimelineFilterService(pageable, wordFilterService, renoteMuteRepository)
        }
    }

    suspend fun filterNotes(notes: List<PlaneNoteViewData>): List<PlaneNoteViewData> {
        return notes.mapNotNull { note ->
            val newResult = when(val result = note.filterResult) {
                PlaneNoteViewData.FilterResult.NotExecuted -> {
                    if (wordFilterService.isShouldFilterNote(pageable, note.note)) {
                        PlaneNoteViewData.FilterResult.ShouldFilterNote
                    } else {
                        if (note.note.note.isRenoteOnly()
                            && renoteMuteRepository.exists(
                                note.note.note.userId
                            ).getOrElse { false }
                        ) {
                            PlaneNoteViewData.FilterResult.ShouldFilterNote
                        } else {
                            PlaneNoteViewData.FilterResult.Pass
                        }
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