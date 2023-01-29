package net.pantasystem.milktea.model.antenna

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountService @Inject constructor(
    val accountRepository: AccountRepository
) {

    suspend fun add(page: Page): Result<Unit> = runCancellableCatching {
        val account: Account = accountRepository.get(page.accountId)
            .getOrNull() ?: accountRepository.getCurrentAccount().getOrThrow()
        val updated = account.copy(pages = account.pages.toMutableList().also { list ->
            list.add(page)
        })
        accountRepository.add(updated, true).getOrThrow()
    }

    suspend fun remove(page: Page): Result<Unit> = runCancellableCatching {
        val account = accountRepository.get(page.accountId)
            .getOrNull() ?: accountRepository.getCurrentAccount().getOrThrow()
        val updated = account.copy(pages = account.pages.filterNot { it.pageId == page.pageId })
        accountRepository.add(updated, true).getOrThrow()
    }

}