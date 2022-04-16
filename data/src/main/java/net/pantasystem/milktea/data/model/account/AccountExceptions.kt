package net.pantasystem.milktea.data.model.account

import java.lang.IllegalStateException

interface AccountException

/**
 * アカウントが存在しなかった場合に呼び出されます
 */
class AccountNotFoundException(msg: String = "") : NoSuchElementException(msg), AccountException{

    companion object{
        @JvmStatic
        private val serialVersionUID = 1L
    }



}

class AccountRegistrationFailedException(msg: String = "アカウントの登録に失敗しました") : IllegalStateException(msg)