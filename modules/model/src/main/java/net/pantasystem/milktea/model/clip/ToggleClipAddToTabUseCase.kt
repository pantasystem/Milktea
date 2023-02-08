package net.pantasystem.milktea.model.clip

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.antenna.AccountService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleClipAddToTabUseCase @Inject constructor(
    private val accountService: AccountService,
    private val accountRepository: AccountRepository,
) : UseCase {

    suspend operator fun invoke(clip: Clip): Result<Unit> = runCancellableCatching {
        val account = accountRepository.get(clip.id.accountId).getOrThrow()
        val existsPage = account.pages.firstOrNull {
            it.pageParams.clipId == clip.id.clipId
        }
        if (existsPage == null) {
            accountService.add(
                Page(
                    title = clip.name,
                    weight = -1,
                    accountId = clip.id.accountId,
                    pageable = Pageable.ClipNotes(
                        clipId = clip.id.clipId,
                    )
                )
            )
        } else {
            accountService.remove(existsPage)
        }
    }
}