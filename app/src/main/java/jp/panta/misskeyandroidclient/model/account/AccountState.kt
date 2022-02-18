package jp.panta.misskeyandroidclient.model.account


sealed interface AccountState {
    data class Authorized(val account: Account) : AccountState {
        fun changeCurrent(account: Account): Authorized {
            return copy(account = account)
        }
    }
    object Loading : AccountState {
        fun authorized(account: Account): Authorized {
            return Authorized(account)
        }
    }
    data class Unauthorized(val error: Throwable? = null) : AccountState {
        fun changeCurrent(account: Account): Authorized {
            return Authorized(account = account)
        }
    }
}