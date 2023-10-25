package net.pantasystem.milktea.data.infrastructure.note

import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UnusedPageReleaserTest {

    @Test
    fun testReleaseUnusedPageWithExistContent() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = 5
        val offset = 3

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(listOf(3, 4, 5, 6, 7, 8), (result?.content as StateContent.Exist).rawContent)
    }

    @Test
    fun testReleaseUnusedPageWithExistContentPositionFirst() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = 0
        val offset = 7

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), (result?.content as StateContent.Exist).rawContent)
    }

    @Test
    fun testReleaseUnusedPageWithExistContentPositionLast() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = 7
        val offset = 3

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(listOf(5, 6, 7, 8, 9, 10), (result?.content as StateContent.Exist).rawContent)
    }
    @Test
    fun testReleaseUnusedPageWithExistContentPositionLastOffset4() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = 7
        val offset = 4

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(listOf(4, 5, 6, 7, 8, 9, 10), (result?.content as StateContent.Exist).rawContent)
    }

    @Test
    fun testReleaseUnusedPageWithNotExistContent() {
        val state = PageableState.Fixed(StateContent.NotExist<List<Int>>())
        val position = 5
        val offset = 3

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNull(result)
    }

    @Test
    fun testReleaseUnusedPageWithSmallDiffCount() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = 5
        val offset = 1

        val result = releaseUnusedPage(state, position, offset, 20)

        Assertions.assertNull(result)
    }

    @Test
    fun testReleaseUnusedPageWithNegativePosition() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = -5
        val offset = 3

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNull(result)
    }

    @Test
    fun testReleaseUnusedPageWithPositionGreaterThanSize() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val state = PageableState.Fixed(StateContent.Exist(items))
        val position = 15
        val offset = 3

        val result = releaseUnusedPage(state, position, offset, 3)

        Assertions.assertNull(result)
    }

}