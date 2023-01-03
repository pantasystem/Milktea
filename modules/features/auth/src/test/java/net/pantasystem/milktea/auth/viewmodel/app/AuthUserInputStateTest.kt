package net.pantasystem.milktea.auth.viewmodel.app

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class AuthUserInputStateTest {

    @Test
    fun getUserName_GiveAcct() {
        val formState = AuthUserInputState(
            appName = "",
            instanceDomain = "https://misskey.io",
            password = "test",
            rawInputInstanceDomain = "@Panta@misskey.io",
            isTermsOfServiceAgreement = false,
            isPrivacyPolicyAgreement = false
        )
        Assertions.assertEquals("Panta", formState.username)
    }

    @Test
    fun getHost_GiveAcct() {
        val formState = AuthUserInputState(
            appName = "",
            instanceDomain = "https://misskey.io",
            password = "test",
            rawInputInstanceDomain = "@Panta@misskey.io",
            isTermsOfServiceAgreement = false,
            isPrivacyPolicyAgreement = false
        )
        Assertions.assertEquals("misskey.io", formState.host)
    }
}