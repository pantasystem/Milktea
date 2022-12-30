package jp.panta.misskeyandroidclient.model.api

import net.pantasystem.milktea.model.instance.Version
import org.junit.jupiter.api.Test


class VersionTest{

    @Test
    fun comparisonVersionTest(){
        val asSmallVersion = Version("11.45.14.30")
        val asLargeVersion = Version("11.46.18")
        assert(asSmallVersion < asLargeVersion)
    }

    @Test
    fun equalSizeVersionTest(){
        val version1 = Version("11.45.14")
        val version2 = Version("11.45.14")
        assert(version1 == version2)
    }

    @Test
    fun rangeTest(){
        val version = Version("12.00.1")

        assert(version > Version("12") && version < Version("13"))
        assert(version.isUntilRange(Version("12"), Version("13")))

    }


}