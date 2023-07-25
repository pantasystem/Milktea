package net.pantasystem.milktea.model.user

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.antenna.AccountService
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

class ToggleUserTimelineAddTabUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val localConfigRepository: LocalConfigRepository,
    private val accountService: AccountService,
) : UseCase {

    suspend operator fun invoke(userId: User.Id): Result<Unit> = runCancellableCatching {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        val page = account.pages.firstOrNull {
            val pageable = it.pageable()
            pageable is Pageable.UserTimeline && pageable.userId == userId.id
        }
        if (page == null) {
            accountService.add(
                PageableTemplate(account).user(
                    userRepository.find(userId),
                    localConfigRepository.get().getOrThrow().isUserNameDefault
                )
            )
        } else {
            accountService.remove(page)
        }
    }
}