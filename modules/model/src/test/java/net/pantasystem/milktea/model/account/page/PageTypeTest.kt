package net.pantasystem.milktea.model.account.page

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PageTypeTest {

    // enumのnameとlabelが一致することを確認する
    @Test
    fun enumNameEqualsLabel() {
        PageType.values().forEach {
            Assertions.assertEquals(it.name, it.label)
        }
    }
}