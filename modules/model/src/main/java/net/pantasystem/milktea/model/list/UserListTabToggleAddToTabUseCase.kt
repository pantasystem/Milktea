package net.pantasystem.milktea.model.list

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.antenna.AccountService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserListTabToggleAddToTabUseCase @Inject constructor(
    private val userListRepository: UserListRepository,
    private val accountRepository: AccountRepository,
    private val accountService: AccountService,
): UseCase {

    suspend operator fun invoke(listId: UserList.Id) = runCancellableCatching<Unit> {
        val account = accountRepository.get(listId.accountId)
            .getOrThrow()
        val page = account.pages.firstOrNull {
            it.pageParams.listId == listId.userListId
        }

        if (page == null) {
            val userList = userListRepository.findOne(listId)
            accountService.add(
                Page(
                    account.accountId,
                    userList.name,
                    weight = -1,
                    pageable = when(account.instanceType) {
                        Account.InstanceType.MISSKEY -> Pageable.UserListTimeline(
                            listId.userListId
                        )
                        Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> Pageable.Mastodon.ListTimeline(
                            listId.userListId
                        )
                    }
                )
            )

        } else {
            accountService.remove(page)
        }

    }
}