package net.pantasystem.milktea.note.editor.emoji

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EmojiTokenScannerTest {

    @Test
    fun findCurrentToken_カーソル直前の入力中トークンを検出する() {
        // ":smi" のカーソルが末尾(4)
        val token = EmojiTokenScanner.findCurrentToken(":smi", 4)
        Assertions.assertEquals(EmojiToken(start = 0, end = 4, query = "smi"), token)
    }

    @Test
    fun findCurrentToken_文中のトークンでも開始位置を正しく返す() {
        // "hello :sm" のカーソルが末尾(9)
        val text = "hello :sm"
        val token = EmojiTokenScanner.findCurrentToken(text, text.length)
        Assertions.assertEquals(EmojiToken(start = 6, end = 9, query = "sm"), token)
    }

    @Test
    fun findCurrentToken_コロン直後でqueryが空ならquotが空文字のトークンを返す() {
        // ":" のカーソルが末尾(1)。query は空。呼び出し側が空を弾く想定。
        val token = EmojiTokenScanner.findCurrentToken(":", 1)
        Assertions.assertEquals(EmojiToken(start = 0, end = 1, query = ""), token)
    }

    @Test
    fun findCurrentToken_コロンが無ければnull() {
        val token = EmojiTokenScanner.findCurrentToken("hello", 5)
        Assertions.assertNull(token)
    }

    @Test
    fun findCurrentToken_トークンと無関係な位置にカーソルがあればnull() {
        // "abc :smile: def" のカーソルが末尾(閉じた絵文字の後の単語)
        val text = "abc :smile: def"
        val token = EmojiTokenScanner.findCurrentToken(text, text.length)
        Assertions.assertNull(token)
    }

    @Test
    fun findCurrentToken_閉じたコロン直後はquery空トークン() {
        // "abc :smile:" のカーソルが閉じ ':' の直後
        val text = "abc :smile:"
        val token = EmojiTokenScanner.findCurrentToken(text, text.length)
        Assertions.assertEquals(EmojiToken(start = 10, end = 11, query = ""), token)
    }

    @Test
    fun findCurrentToken_名前に許容される記号を含む() {
        // "+1" や "_" を含む絵文字名
        val text = ":plus_1-a"
        val token = EmojiTokenScanner.findCurrentToken(text, text.length)
        Assertions.assertEquals(EmojiToken(start = 0, end = 9, query = "plus_1-a"), token)
    }

    @Test
    fun findCurrentToken_カーソルがトークン途中なら途中までをqueryとする() {
        // ":smile" のカーソルが 3 (":sm"の後)
        val token = EmojiTokenScanner.findCurrentToken(":smile", 3)
        Assertions.assertEquals(EmojiToken(start = 0, end = 3, query = "sm"), token)
    }

    @Test
    fun findCurrentToken_範囲外カーソルはnull() {
        Assertions.assertNull(EmojiTokenScanner.findCurrentToken(":sm", -1))
        Assertions.assertNull(EmojiTokenScanner.findCurrentToken(":sm", 4))
    }
}
