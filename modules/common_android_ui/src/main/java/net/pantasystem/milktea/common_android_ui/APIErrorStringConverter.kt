package net.pantasystem.milktea.common_android_ui

import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common_android.resource.StringSource
import javax.inject.Inject

class APIErrorStringConverter @Inject constructor() {

    operator fun invoke(error: APIError): StringSource {
        return when(error) {
            is APIError.AuthenticationException -> StringSource(R.string.unauthorized_error)
            is APIError.ClientException -> StringSource(R.string.parameter_error)
            is APIError.ForbiddenException -> StringSource(R.string.auth_error)
            is APIError.IAmAIException -> StringSource(R.string.bot_error)
            is APIError.InternalServerException -> StringSource(R.string.server_error)
            is APIError.NotFoundException -> StringSource(R.string.not_found_error)
            is APIError.SomethingException -> {
                if (error.statusCode >= 500) {
                    StringSource(R.string.server_error)
                } else {
                    StringSource("error :${error.statusCode}")
                }
            }
            is APIError.ToManyRequestsException -> StringSource(R.string.rate_limit_error)
        }
    }
}