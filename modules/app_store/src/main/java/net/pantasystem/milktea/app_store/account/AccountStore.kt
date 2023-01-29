package net.pantasystem.milktea.app_store.account


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.MakeDefaultPagesUseCase
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.instance.MetaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStore @Inject constructor(
    val accountRepository: AccountRepository,
    val metaRepository: MetaRepository,
    val loggerFactory: Logger.Factory,
    val makeDefaultPagesUseCase: MakeDefaultPagesUseCase,
) {
    val logger = loggerFactory.create("AccountStore")
    private val _state = MutableStateFlow<AccountState>(AccountState())
    val state: StateFlow<AccountState> = _state

    val currentAccount: Account?
        get() = state.value.currentAccount

    val currentAccountId: Long?
        get() = currentAccount?.accountId

    val observeCurrentAccount: Flow<Account?>
        get() = state.map { it.currentAccount }
    val observeAccounts: Flow<List<Account>>
        get() = state.map { it.accounts }

    init {
        accountRepository.addEventListener {
            when (it) {
                is AccountRepository.Event.Created -> {
                    _state.value = state.value.add(it.account)
                }
                is AccountRepository.Event.Deleted -> {
                    _state.value = state.value.delete(it.accountId)
                }
                is AccountRepository.Event.Updated -> {
                    _state.value = state.value.add(it.account)
                }
            }
        }
    }


    suspend fun addAccount(account: Account) {
        try {
            val newAccount = accountRepository.add(account, true).getOrThrow()
            saveDefaultPages(newAccount)
            val updatedAccount = accountRepository.get(newAccount.accountId).getOrThrow()
            setCurrent(updatedAccount)
        } catch (e: Exception) {
            logger.error("アカウントの追加に失敗しました。", e)
        }
    }

    suspend fun setCurrent(account: Account) {
        accountRepository.setCurrentAccount(account).getOrThrow()
        _state.value = state.value.setCurrentAccount(account)
    }

    suspend fun addPage(page: Page): Boolean {
        val account = _state.value.get(page.accountId)
            ?: _state.value.currentAccount
            ?: throw IllegalArgumentException()
        val updated = account.copy(pages = account.pages.toMutableList().also { list ->
            list.add(page)
        })
        _state.value = _state.value.add(accountRepository.add(updated, true).getOrThrow())
        initialize()

        return true
    }

    suspend fun replaceAllPage(pages: List<Page>): Result<Account> {
        return runCancellableCatching {
            val account = _state.value.currentAccount
                ?: throw IllegalStateException()
            val updated = account.copy(pages = pages)
            val result = accountRepository.add(updated, true).getOrThrow()
            initialize()
            result
        }

    }

    suspend fun removePage(page: Page): Boolean {
        val account = _state.value.get(page.accountId)
            ?: _state.value.currentAccount
            ?: return false
        val updated = account.copy(pages = account.pages.filterNot { it.pageId == page.pageId })
        _state.value = _state.value.add(accountRepository.add(updated, true).getOrThrow())
        initialize()
        return true
    }


    suspend fun initialize() {
        try {
            var current: Account
            var accounts: List<Account>
            try {
                current = accountRepository.getCurrentAccount().getOrThrow()
                accounts = accountRepository.findAll().getOrThrow()
            } catch (e: AccountNotFoundException) {
                _state.value = AccountState(isLoading = false)
                return
            }

            logger.debug { "accountId:${current.accountId}, account:$current" }
            if (current.pages.isEmpty()) {
                saveDefaultPages(current)
                accounts = accountRepository.findAll().getOrThrow()
                current = accountRepository.getCurrentAccount().getOrThrow()
            }


            _state.value = AccountState(
                accounts = accounts,
                currentAccountId = current.accountId,
                isLoading = false
            )
        } catch (e: Exception) {
            //isSuccessCurrentAccount.postValue(false)
            logger.error("初期読み込みに失敗しまちた", e)
            _state.value = state.value.copy(error = e)
        } finally {
            _state.value = state.value.copy(isLoading = false)
        }
    }


    private suspend fun saveDefaultPages(account: Account) {
        if (account.pages.isNotEmpty()) {
            return
        }
        try {
            val meta = metaRepository.find(account.normalizedInstanceDomain).getOrNull()
            val pages = makeDefaultPagesUseCase(account, meta)
            accountRepository.add(account.copy(pages = pages), true)
        } catch (e: Exception) {
            logger.error("default pages create error", e)
        }
    }
}