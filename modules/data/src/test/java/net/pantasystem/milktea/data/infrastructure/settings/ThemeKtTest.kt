package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.Theme
import org.junit.Assert
import org.junit.Test

class ThemeKtTest {

    @Test
    fun toInt() {
        Assert.assertEquals(0, Theme.White.toInt())
        Assert.assertEquals(1, Theme.Black.toInt())
        Assert.assertEquals(2, Theme.Dark.toInt())
        Assert.assertEquals(3, Theme.Bread.toInt())

    }

    @Test
    fun from() {
        Assert.assertEquals(Theme.White, Theme.from(Theme.White.toInt()))
        Assert.assertEquals(Theme.Black, Theme.from(Theme.Black.toInt()))
        Assert.assertEquals(Theme.Dark, Theme.from(Theme.Dark.toInt()))
        Assert.assertEquals(Theme.Bread, Theme.from(Theme.Bread.toInt()))
        Assert.assertEquals(Theme.White,  Theme.from(206))
    }
}