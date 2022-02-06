package jp.panta.misskeyandroidclient.model

interface UseCase<T> {
    suspend fun execute(): T
}