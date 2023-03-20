package net.pantasystem.milktea.data.infrastructure.account.page.db

import net.pantasystem.milktea.model.account.page.PageParams
import net.pantasystem.milktea.model.account.page.PageType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PageRecordParamsTest {

    @Test
    fun convertFromPageParams() {
        val params = PageParams(
            type = PageType.ANTENNA,
            withFiles = false,
            excludeNsfw = true,
            includeLocalRenotes = false,
            includeMyRenotes = true,
            includeRenotedMyRenotes = false,
            listId = "listId",
            following = true,
            visibility = "visibility",
            noteId = "noteId",
            tag = "tag",
            reply = false,
            renote = true,
            poll = false,
            offset = 1,
            markAsRead = true,
            userId = "userId",
            includeReplies = false,
            query = "query",
            host = "host",
            antennaId = "antennaId",
            channelId = "channelId",
            clipId = "clipId",
        )

        val record = PageRecordParams.from(params)
        Assertions.assertEquals(params.type, record.type)
        Assertions.assertEquals(params.withFiles, record.withFiles)
        Assertions.assertEquals(params.excludeNsfw, record.excludeNsfw)
        Assertions.assertEquals(params.includeLocalRenotes, record.includeLocalRenotes)
        Assertions.assertEquals(params.includeMyRenotes, record.includeMyRenotes)
        Assertions.assertEquals(params.includeRenotedMyRenotes, record.includeRenotedMyRenotes)
        Assertions.assertEquals(params.listId, record.listId)
        Assertions.assertEquals(params.following, record.following)
        Assertions.assertEquals(params.visibility, record.visibility)
        Assertions.assertEquals(params.noteId, record.noteId)
        Assertions.assertEquals(params.tag, record.tag)
        Assertions.assertEquals(params.reply, record.reply)
        Assertions.assertEquals(params.renote, record.renote)
        Assertions.assertEquals(params.poll, record.poll)
        Assertions.assertEquals(params.offset, record.offset)
        Assertions.assertEquals(params.markAsRead, record.markAsRead)
        Assertions.assertEquals(params.userId, record.userId)
        Assertions.assertEquals(params.includeReplies, record.includeReplies)
        Assertions.assertEquals(params.query, record.query)
        Assertions.assertEquals(params.host, record.host)
        Assertions.assertEquals(params.antennaId, record.antennaId)
        Assertions.assertEquals(params.channelId, record.channelId)
        Assertions.assertEquals(params.clipId, record.clipId)
    }


    @Test
    fun convertFromPageRecordParams() {
        val recordParams = PageRecordParams(
            type = PageType.ANTENNA,
            withFiles = true,
            excludeNsfw = false,
            includeLocalRenotes = true,
            includeMyRenotes = false,
            includeRenotedMyRenotes = true,
            listId = "listId",
            following = false,
            visibility = "visibility",
            noteId = "noteId",
            tag = "tag",
            reply = true,
            renote = false,
            poll = true,
            offset = 1,
            markAsRead = false,
            userId = "userId",
            includeReplies = true,
            query = "query",
            host = "host",
            antennaId = "antennaId",
            channelId = "channelId",
            clipId = "clipId",
        )

        val model = recordParams.toParams()

        Assertions.assertEquals(recordParams.type, model.type)
        Assertions.assertEquals(recordParams.withFiles, model.withFiles)
        Assertions.assertEquals(recordParams.excludeNsfw, model.excludeNsfw)
        Assertions.assertEquals(recordParams.includeLocalRenotes, model.includeLocalRenotes)
        Assertions.assertEquals(recordParams.includeMyRenotes, model.includeMyRenotes)
        Assertions.assertEquals(recordParams.includeRenotedMyRenotes, model.includeRenotedMyRenotes)
        Assertions.assertEquals(recordParams.listId, model.listId)
        Assertions.assertEquals(recordParams.following, model.following)
        Assertions.assertEquals(recordParams.visibility, model.visibility)
        Assertions.assertEquals(recordParams.noteId, model.noteId)
        Assertions.assertEquals(recordParams.tag, model.tag)
        Assertions.assertEquals(recordParams.reply, model.reply)
        Assertions.assertEquals(recordParams.renote, model.renote)
        Assertions.assertEquals(recordParams.poll, model.poll)
        Assertions.assertEquals(recordParams.offset, model.offset)
        Assertions.assertEquals(recordParams.markAsRead, model.markAsRead)
        Assertions.assertEquals(recordParams.userId, model.userId)
        Assertions.assertEquals(recordParams.includeReplies, model.includeReplies)
        Assertions.assertEquals(recordParams.query, model.query)
        Assertions.assertEquals(recordParams.host, model.host)
        Assertions.assertEquals(recordParams.channelId, model.channelId)
        Assertions.assertEquals(recordParams.antennaId, model.antennaId)
        Assertions.assertEquals(recordParams.clipId, model.clipId)
    }
}