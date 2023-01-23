package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.Theme
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ThemeKtTest {

    @Test
    fun toInt() {
        Assertions.assertEquals(0, Theme.White.toInt())
        Assertions.assertEquals(1, Theme.Black.toInt())
        Assertions.assertEquals(2, Theme.Dark.toInt())
        Assertions.assertEquals(3, Theme.Bread.toInt())

    }

    @Test
    fun from() {
        Assertions.assertEquals(Theme.White, Theme.from(Theme.White.toInt()))
        Assertions.assertEquals(Theme.Black, Theme.from(Theme.Black.toInt()))
        Assertions.assertEquals(Theme.Dark, Theme.from(Theme.Dark.toInt()))
        Assertions.assertEquals(Theme.Bread, Theme.from(Theme.Bread.toInt()))
        Assertions.assertEquals(Theme.White, Theme.from(206))
    }
}