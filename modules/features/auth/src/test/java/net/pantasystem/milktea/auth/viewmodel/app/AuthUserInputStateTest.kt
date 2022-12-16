package net.pantasystem.milktea.auth.viewmodel.app

import org.junit.Assert
import org.junit.Test

class AuthUserInputStateTest {

    @Test
    fun getUserName_GiveAcct() {
        val formState = AuthUserInputState(
            appName = "",
            instanceDomain = "https://misskey.io",
            password = "test",
            rawInputInstanceDomain = "@Panta@misskey.io"
        )
        Assert.assertEquals("Panta", formState.username)
    }

    @Test
    fun getHost_GiveAcct() {
        val formState = AuthUserInputState(
            appName = "",
            instanceDomain = "https://misskey.io",
            password = "test",
            rawInputInstanceDomain = "@Panta@misskey.io"
        )
        Assert.assertEquals("misskey.io", formState.host)
    }
}