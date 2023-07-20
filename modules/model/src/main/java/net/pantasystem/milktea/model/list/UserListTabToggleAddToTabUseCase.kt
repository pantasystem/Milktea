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

    suspend operator fun invoke(listId: UserList.Id, addTabToAccountId: Long? = null) = runCancellableCatching<Unit> {
        val account = accountRepository.get(addTabToAccountId ?: listId.accountId)
            .getOrThrow()
        val page = account.pages.firstOrNull { page ->
            page.pageParams.listId == listId.userListId
                    && listId.accountId == (page.attachedAccountId ?: page.accountId)
        }

        val relatedAccount = accountRepository.get(listId.accountId).getOrThrow()

        if (page == null) {
            val userList = userListRepository.findOne(listId)
            accountService.add(
                Page(
                    account.accountId,
                    if (addTabToAccountId == null) userList.name else "${userList.name}(${relatedAccount.getAcct()})",
                    weight = -1,
                    attachedAccountId = if (addTabToAccountId == null) null else relatedAccount.accountId,
                    pageable = when(account.instanceType) {
                        Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> Pageable.UserListTimeline(
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