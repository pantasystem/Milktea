package jp.panta.misskeyandroidclient.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


suspend fun<T> (suspend () -> T).asLoadingStateFlow(): Flow<State<T>> {

    return flow {
        emit(State.Fixed(StateContent.NotExist()))
        emit(State.Loading(StateContent.NotExist()))
        runCatching {
            this@asLoadingStateFlow.invoke()
        }.onSuccess {
            emit(State.Fixed(StateContent.Exist(it)))
        }.onFailure {
            emit(State.Error(StateContent.NotExist(), it))
        }
    }
}