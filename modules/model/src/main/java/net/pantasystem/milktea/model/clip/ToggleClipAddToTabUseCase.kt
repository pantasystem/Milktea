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

    suspend operator fun invoke(clip: Clip, addTabToAccountId: Long?): Result<Unit> = runCancellableCatching {
        val account = addTabToAccountId?.let {
            accountRepository.get(it).getOrThrow()
        } ?: accountRepository.get(clip.id.accountId).getOrThrow()
        val existsPage = account.pages.firstOrNull {
            it.pageParams.clipId == clip.id.clipId
                    && (it.attachedAccountId ?: it.accountId) == clip.id.accountId
        }
        val isSameAccount = account.accountId == clip.id.accountId
        val relatedAccount = if (isSameAccount) {
            account
        } else {
            accountRepository.get(clip.id.accountId).getOrThrow()
        }
        val title = if (isSameAccount) {
            clip.name
        } else {
            "${clip.name}(${relatedAccount.getAcct()})"
        }
        if (existsPage == null) {
            accountService.add(
                Page(
                    title = title,
                    weight = -1,
                    accountId = addTabToAccountId ?: clip.id.accountId,
                    pageable = Pageable.ClipNotes(
                        clipId = clip.id.clipId,
                    ),
                    attachedAccountId = if (isSameAccount) null else relatedAccount.accountId,
                )
            )
        } else {
            accountService.remove(existsPage)
        }
    }
}