package net.pantasystem.milktea.note.timeline.viewmodel.filter

import net.pantasystem.milktea.model.account.page.CanExcludeReplies
import net.pantasystem.milktea.model.account.page.CanExcludeReposts
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache

class ExcludeRepostOrReplyFilter(
    val pageable: Pageable,
) : PlaneNoteViewDataCache.ViewDataFilter {
    override suspend fun check(viewData: PlaneNoteViewData): PlaneNoteViewData.FilterResult {
        if (
            (pageable as? CanExcludeReplies<*>)?.getExcludeReplies() == true
            && viewData.note.note.replyId != null
        ) {
            return PlaneNoteViewData.FilterResult.ShouldFilterNote
        }

        if ((pageable as? CanExcludeReposts<*>)?.getExcludeReposts() == true
            && viewData.note.note.isRenoteOnly()
        ) {
            return PlaneNoteViewData.FilterResult.ShouldFilterNote
        }
        return viewData.filterResult
    }
}