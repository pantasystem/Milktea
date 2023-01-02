package net.pantasystem.milktea.auth.viewmodel.app

import net.pantasystem.milktea.api.mastodon.instance.Instance
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.model.instance.InstanceInfo
import net.pantasystem.milktea.model.instance.Meta


data class AuthUserInputState(
    val instanceDomain: String,
    val rawInputInstanceDomain: String,
    val appName: String,
    val password: String,
    val isPrivacyPolicyAgreement: Boolean,
    val isTermsOfServiceAgreement: Boolean,
) {
    val isIdPassword: Boolean by lazy {
        userNameRegex.matches(rawInputInstanceDomain)
    }

    val username: String? by lazy {
        runCancellableCatching {
            userNameRegex.find(rawInputInstanceDomain)?.groups?.get(1)
        }.getOrNull()?.value
    }

    val host: String? by lazy {
        runCancellableCatching {
            userNameRegex.find(rawInputInstanceDomain)?.groups?.get(2)
        }.getOrNull()?.value
    }

}

data class BeforeAuthState(
    val inputState: AuthUserInputState,
    val meta: ResultState<InstanceType>,
)


sealed interface InstanceType {
    data class Mastodon(val instance: Instance) : InstanceType
    data class Misskey(val instance: Meta) : InstanceType
}

sealed interface GenerateTokenResult {
    object Success : GenerateTokenResult
    object Fixed : GenerateTokenResult
    object Failure : GenerateTokenResult

}

val userNameRegex = Regex("""\A@([\w._\-]+)@([\w._\-]+)""")

data class AuthUiState(
    val formState: AuthUserInputState,
    val metaState: ResultState<InstanceType>,
    val stateType: Authorization,
    val waiting4ApproveState: ResultState<Authorization.Waiting4UserAuthorization> = ResultState.Fixed(
        StateContent.NotExist()),
    val clientId: String = "",
    val instances: List<InstanceInfo> = emptyList(),
) {
    val isProgress by lazy {
        metaState is ResultState.Loading || waiting4ApproveState is ResultState.Loading
    }


}