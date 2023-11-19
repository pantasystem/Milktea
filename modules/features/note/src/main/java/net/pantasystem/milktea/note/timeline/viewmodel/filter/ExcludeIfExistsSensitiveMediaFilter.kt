package net.pantasystem.milktea.note.timeline.viewmodel.filter

import net.pantasystem.milktea.model.account.page.CanExcludeIfExistsSensitiveMedia
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache

class ExcludeIfExistsSensitiveMediaFilter(private val pageable: Pageable, private val configRepository: LocalConfigRepository) : PlaneNoteViewDataCache.ViewDataFilter {

    override suspend fun check(viewData: PlaneNoteViewData): PlaneNoteViewData.FilterResult {
        // 自分の投稿ではないことを確認(リノートや返信も含む)
        if (viewData.note.user.id.id == viewData.account.remoteId) {
            // 何もしない
            return viewData.filterResult
        }

        val config = configRepository.get().getOrElse { DefaultConfig.config }

        // 除外設定がされていない場合は何もしない
        if (!config.isEnableSafeSearch && (pageable as? CanExcludeIfExistsSensitiveMedia<*>)?.getExcludeIfExistsSensitiveMedia() != true) {
            return viewData.filterResult
        }

        val hasSensitive = viewData.toShowNote.files?.any { file ->
            file.isSensitive
        } ?: false

        val hasSensitiveInRenoteToNote = viewData.toShowNote.files?.any { file ->
            file.isSensitive
        } ?: false

        if (hasSensitive || hasSensitiveInRenoteToNote) {
            return PlaneNoteViewData.FilterResult.ShouldFilterNote
        }

        return viewData.filterResult
    }
}