package net.pantasystem.milktea.model.account.page

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PageableTest {

    @Test
    fun setOnlyMedia_GiveGlobalTimeline() {
        val pageable = Pageable.GlobalTimeline()
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveLocalTimeline() {
        val pageable = Pageable.LocalTimeline()
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveHybridTimeline() {
        val pageable = Pageable.HybridTimeline()
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveHomeTimeline() {
        val pageable = Pageable.HomeTimeline()
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveUserListTimeline() {
        val pageable = Pageable.UserListTimeline(listId = "")
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveSearchByTag() {
        val pageable = Pageable.SearchByTag("")
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveUserTimeline() {
        val pageable = Pageable.UserTimeline("")
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).withFiles)
        Assertions.assertNull(pageable.setOnlyMedia(false).withFiles)
        Assertions.assertEquals(false, pageable.copy(withFiles = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveMastodonPublicTimeline() {
        val pageable = Pageable.Mastodon.PublicTimeline()
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).isOnlyMedia)
        Assertions.assertEquals(false, pageable.setOnlyMedia(false).isOnlyMedia)
        Assertions.assertEquals(false, pageable.copy(isOnlyMedia = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveMastodonLocalTimeline() {
        val pageable = Pageable.Mastodon.LocalTimeline()
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).isOnlyMedia)
        Assertions.assertEquals(false, pageable.setOnlyMedia(false).isOnlyMedia)
        Assertions.assertEquals(false, pageable.copy(isOnlyMedia = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveMastodonHashTagTimeline() {
        val pageable = Pageable.Mastodon.HashTagTimeline("")
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).isOnlyMedia)
        Assertions.assertEquals(false, pageable.setOnlyMedia(false).isOnlyMedia)
        Assertions.assertEquals(false, pageable.copy(isOnlyMedia = null).getOnlyMedia())
    }

    @Test
    fun setOnlyMedia_GiveMastodonUserTimeline() {
        val pageable = Pageable.Mastodon.UserTimeline("")
        Assertions.assertEquals(true, pageable.setOnlyMedia(true).isOnlyMedia)
        Assertions.assertEquals(false, pageable.setOnlyMedia(false).isOnlyMedia)
        Assertions.assertEquals(false, pageable.copy(isOnlyMedia = null).getOnlyMedia())
    }
}