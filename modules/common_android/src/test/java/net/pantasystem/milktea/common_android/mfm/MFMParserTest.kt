package net.pantasystem.milktea.common_android.mfm

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class MFMParserTest {

    @Test
    fun getMentionHost_SameMentionHostAndUserOtherHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.io",
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assertions.assertEquals("", host)
    }

    @Test
    fun getMentionHost_GiveOtherHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.pantasystem.com",
            accountHost = "misskey.io",
            userHost = "misskey.pantasystem.com"
        )
        Assertions.assertEquals("@misskey.pantasystem.com", host)
    }

    @Test
    fun getMentionHost_GiveOtherUserHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = null,
            accountHost = "misskey.io",
            userHost = "misskey.pantasystem.com"
        )
        Assertions.assertEquals("@misskey.pantasystem.com", host)
    }

    @Test
    fun getMentionHost_GiveSameAccountAndUserAndMention() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.io",
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assertions.assertEquals("", host)
    }

    @Test
    fun getMentionHost_GiveNullMentionHostAndSameAccountAndUserHost() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = null,
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assertions.assertEquals("", host)
    }


    @Test
    fun getMentionHost_Pattern1() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@pokemon.mastportal.info",
            accountHost = "misskey.io",
            userHost = "mstdn.jp"
        )
        Assertions.assertEquals("@pokemon.mastportal.info", host)
    }

    @Test
    fun mentionPattern() {
        val matcher = MFMParser.mentionPattern.matcher("@Panta@pokemon.mastportal.info")
        Assertions.assertTrue(matcher.find())
        Assertions.assertEquals("@pokemon.mastportal.info", matcher.group(2))
    }

    @Test
    fun getMentionHost_GiveSameUserAndAccountHostInMentionHostOtherInstance() {
        val host = MFMParser.getMentionHost(
            hostInMentionText = "@misskey.pantasystem.com",
            accountHost = "misskey.io",
            userHost = "misskey.io"
        )
        Assertions.assertEquals("@misskey.pantasystem.com", host)
    }

    @Test
    fun convertAppNoteUriIfGiveNoteUrl_GiveSameHostAndNoteUrl() {
        val result = MFMParser.convertAppNoteUriIfGiveNoteUrl("misskey.pantasystem.com", "https://misskey.pantasystem.com/notes/hoaA8u0faf")
        Assertions.assertEquals("milktea://notes/hoaA8u0faf", result)
    }

    @Test
    fun convertAppNoteUriIfGiveNoteUrl_GiveBlankId() {
        val result = MFMParser.convertAppNoteUriIfGiveNoteUrl("misskey.pantasystem.com", "https://misskey.pantasystem.com/notes/")
        Assertions.assertNull(result)
    }

    @Test
    fun convertAppChannelUriIfGiveChannelUrl_GiveSameHostAndChannelUrl() {
        val result = MFMParser.convertAppChannelUriIfGiveChannelUrl("misskey.pantasystem.com", "https://misskey.pantasystem.com/channels/hgeoa390fj")
        Assertions.assertEquals("milktea://channels/hgeoa390fj", result)
    }

    @Test
    fun convertAppChannelUrlIfGiveChannelUrl_GiveBlankId() {
        val result = MFMParser.convertAppChannelUriIfGiveChannelUrl("misskey.pantasystem.com", "https://misskey.pantasystem.com/channels/")
        Assertions.assertNull(result)
    }

    @Test
    fun convertAppChannelUriIfGiveChannelUrl_GiveNotesUrlReturnsNull() {
        val result = MFMParser.convertAppChannelUriIfGiveChannelUrl("misskey.io", "https://misskey.io/notes/hoaA8u0faf")
        Assertions.assertNull(result)
    }

}