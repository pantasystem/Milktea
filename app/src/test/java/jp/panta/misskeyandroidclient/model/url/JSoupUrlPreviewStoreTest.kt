package jp.panta.misskeyandroidclient.model.url

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

class JSoupUrlPreviewStoreTest {

    @Test
    fun get() {
        val store = JSoupUrlPreviewStore()
        val data = store.
            get("https://digitalidentity.co.jp/blog/seo/ogp-share-setting.html")
        println(data)
        assertNotEquals(data, null)
    }

    @Test
    fun getTest2(){
        val store = JSoupUrlPreviewStore()
        val data = store.get("https://misskey.io/notes/87myil8pmf")
        println(data)
        assertNotEquals(data, null)
    }

    @Test
    fun getTest3(){
        val store = JSoupUrlPreviewStore()
        val data = store.get("https://qiita.com/azukiazusa/items/8238c0c68ed525377883")
        println(data)
        assertNotEquals(data, null)
    }

    @Test
    fun getTest4(){
        val store = JSoupUrlPreviewStore()
        val data = store.get("https://www.youtube.com/watch?v=piSLPUjwPdQ")
        println(data)
        assertNotEquals(data, null)
    }
}