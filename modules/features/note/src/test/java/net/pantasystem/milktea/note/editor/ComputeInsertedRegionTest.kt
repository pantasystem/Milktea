package net.pantasystem.milktea.note.editor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ComputeInsertedRegionTest {

    @Test
    fun 末尾への貼り付けを検出する() {
        val region = computeInsertedRegion("hello ", "hello https://example.com")
        Assertions.assertEquals(
            InsertedRegion(start = 6, text = "https://example.com"),
            region,
        )
    }

    @Test
    fun 文中への挿入を検出する() {
        val region = computeInsertedRegion("ab", "aXYZb")
        Assertions.assertEquals(InsertedRegion(start = 1, text = "XYZ"), region)
    }

    @Test
    fun 先頭への挿入を検出する() {
        val region = computeInsertedRegion("b", "Ab")
        Assertions.assertEquals(InsertedRegion(start = 0, text = "A"), region)
    }

    @Test
    fun 短くなった場合はnull() {
        Assertions.assertNull(computeInsertedRegion("hello", "hell"))
    }

    @Test
    fun 同一文字列はnull() {
        Assertions.assertNull(computeInsertedRegion("hello", "hello"))
    }

    @Test
    fun 空から貼り付け() {
        val region = computeInsertedRegion("", "https://example.com")
        Assertions.assertEquals(
            InsertedRegion(start = 0, text = "https://example.com"),
            region,
        )
    }
}
