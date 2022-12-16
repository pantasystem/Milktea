package net.pantasystem.milktea.common_android.mfm

import org.junit.Assert
import org.junit.Test

class MFMParserTest {

    @Test
    fun getMentionHost_SameMentionHostAndUserOtherHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.io",
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assert.assertEquals("", host)
    }

    @Test
    fun getMentionHost_GiveOtherHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.pantasystem.com",
            accountHost = "misskey.io",
            userHost = "misskey.pantasystem.com"
        )
        Assert.assertEquals("@misskey.pantasystem.com", host)
    }

    @Test
    fun getMentionHost_GiveOtherUserHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = null,
            accountHost = "misskey.io",
            userHost = "misskey.pantasystem.com"
        )
        Assert.assertEquals("@misskey.pantasystem.com", host)
    }

    @Test
    fun getMentionHost_GiveSameAccountAndUserAndMention() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.io",
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assert.assertEquals("", host)
    }

    @Test
    fun getMentionHost_GiveNullMentionHostAndSameAccountAndUserHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = null,
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assert.assertEquals("", host)
    }


    @Test
    fun getMentionHost_Pattern1() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@pokemon.mastportal.info",
            accountHost = "misskey.io",
            userHost = "mstdn.jp"
        )
        Assert.assertEquals("@pokemon.mastportal.info", host)
    }

    @Test
    fun mentionPattern() {
        val matcher = MFMParser.mentionPattern.matcher("@Panta@pokemon.mastportal.info")
        Assert.assertTrue(matcher.find())
        Assert.assertEquals("@pokemon.mastportal.info", matcher.group(2))
    }

}