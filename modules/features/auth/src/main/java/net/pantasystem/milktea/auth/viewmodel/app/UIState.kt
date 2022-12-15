package net.pantasystem.milktea.auth.viewmodel.app

import net.pantasystem.milktea.api.mastodon.instance.Instance
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.model.instance.Meta


data class AuthUserInputState(
    val instanceDomain: String,
    val appName: String
)

data class BeforeAuthState(
    val inputState: AuthUserInputState,
    val meta: ResultState<InstanceType>,
)


sealed interface AuthErrors {
    val throwable: Throwable

    data class GetMetaError(
        override val throwable: Throwable,
    ) : AuthErrors

    data class GenerateTokenError(
        override val throwable: Throwable
    ) : AuthErrors
}

sealed interface InstanceType {
    data class Mastodon(val instance: Instance) : InstanceType
    data class Misskey(val instance: Meta) : InstanceType
}

data class AuthUiState(
    val formState: AuthUserInputState,
    val metaState: ResultState<InstanceType>,
    val stateType: Authorization,
    val waiting4ApproveState: ResultState<Authorization.Waiting4UserAuthorization> = ResultState.Fixed(
        StateContent.NotExist())
) {
    val isProgress by lazy {
        metaState is ResultState.Loading || waiting4ApproveState is ResultState.Loading
    }

    val validatedInstanceDomain by lazy {
        metaState is ResultState.Fixed && metaState.content is StateContent.Exist
    }

    val errors by lazy {
        when {
            waiting4ApproveState is ResultState.Error -> {
                AuthErrors.GenerateTokenError(waiting4ApproveState.throwable)
            }
            metaState is ResultState.Error -> {
                AuthErrors.GetMetaError(metaState.throwable)
            }
            else -> {
                null
            }
        }
    }
}