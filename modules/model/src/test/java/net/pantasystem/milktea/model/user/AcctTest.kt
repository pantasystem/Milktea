package net.pantasystem.milktea.model.user

import org.junit.Assert
import org.junit.Test

class AcctTest {

    @Test
    fun userName_giveUserNameOnly() {
        val textAcct = "@Panta"
        val acct = Acct(textAcct)
        Assert.assertEquals("Panta", acct.userName)
    }

    @Test
    fun host_giveUserNameOnlyReturnsNull() {
        val textAcct = "@Panta"
        val acct = Acct(textAcct)
        Assert.assertNull(acct.host)
    }

    @Test
    fun userName_giveUserNameAndHost() {
        val textAcct = "@Panta@misskey.io"
        val acct = Acct(textAcct)
        Assert.assertEquals("Panta", acct.userName)
    }

    @Test
    fun host_giveUserNameAndHost() {
        val textAcct = "@Panta@misskey.io"
        val acct = Acct(textAcct)
        Assert.assertEquals("misskey.io", acct.host)
    }
}