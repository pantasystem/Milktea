package net.pantasystem.milktea.model.account

interface AccountException

/**
 * アカウントが存在しなかった場合に呼び出されます
 */
class AccountNotFoundException(accountId: Long? = null) : NoSuchElementException("Account not found: $accountId"), AccountException {

    companion object{
        @JvmStatic
        private val serialVersionUID = 1L
    }



}

class AccountRegistrationFailedException(msg: String = "アカウントの登録に失敗しました") : IllegalStateException(msg)