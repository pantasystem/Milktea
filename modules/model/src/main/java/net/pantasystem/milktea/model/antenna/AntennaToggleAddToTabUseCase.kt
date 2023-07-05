package net.pantasystem.milktea.model.antenna

import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.PageableTemplate
import javax.inject.Inject

class AntennaToggleAddToTabUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountService: AccountService,
) {
    suspend operator fun invoke(antenna: Antenna, addTabToAccountId: Long? = null) {
        val relatedAccount = accountRepository.get(antenna.id.accountId).getOrThrow()
        val current = addTabToAccountId?.let {
            accountRepository.get(it).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow()
        val paged = current.pages.firstOrNull {
            it.pageParams.antennaId == antenna.id.antennaId
                    && (it.attachedAccountId ?: it.accountId) == antenna.id.accountId
        }
        val isSameAccount = relatedAccount.accountId == current.accountId
        val title = if (isSameAccount) {
            antenna.name
        } else {
            "${antenna.name}(${relatedAccount.getAcct()})"
        }
        if (paged == null) {
            val page = PageableTemplate(current)
                .antenna(
                    antenna
                ).copy(
                    attachedAccountId = if (isSameAccount) null else relatedAccount.accountId,
                    title = title,
                    weight = -1,
                    accountId = current.accountId,
                )
            accountService.add(
                page
            )
        } else {
            accountService.remove(paged)
        }
    }
}