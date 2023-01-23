package net.pantasystem.milktea.model.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class AcctTest {

    @Test
    fun userName_giveUserNameOnly() {
        val textAcct = "@Panta"
        val acct = Acct(textAcct)
        Assertions.assertEquals("Panta", acct.userName)
    }

    @Test
    fun host_giveUserNameOnlyReturnsNull() {
        val textAcct = "@Panta"
        val acct = Acct(textAcct)
        Assertions.assertNull(acct.host)
    }

    @Test
    fun userName_giveUserNameAndHost() {
        val textAcct = "@Panta@misskey.io"
        val acct = Acct(textAcct)
        Assertions.assertEquals("Panta", acct.userName)
    }

    @Test
    fun host_giveUserNameAndHost() {
        val textAcct = "@Panta@misskey.io"
        val acct = Acct(textAcct)
        Assertions.assertEquals("misskey.io", acct.host)
    }
}