package net.pantasystem.milktea.note.editor.emoji

/**
 * カーソル直前で入力中のカスタム絵文字トークン（`:query`）を表す。
 *
 * @param start トークン開始位置。`:` そのものの index（置換範囲の開始）。
 * @param end トークン終了位置。通常はカーソル位置（置換範囲の終了・排他的）。
 * @param query `:` の直後からカーソルまでの文字列（絵文字名の入力途中）。
 */
data class EmojiToken(
    val start: Int,
    val end: Int,
    val query: String,
)

/**
 * テキストとカーソル位置から、入力途中のカスタム絵文字トークンを検出する純粋ロジック。
 *
 * 旧実装の `CustomEmojiTokenizer`（MultiAutoCompleteTextView.Tokenizer）に相当するが、
 * Compose では補完候補の置換を自前で行うため、必要なのは「カーソル直前のトークン検出」だけ。
 *
 * 絵文字名として許容する文字は英数字・`_`・`+`・`-`。カーソルから後方へ名前文字を辿り、
 * その直前が `:` であればトークンとみなす。
 */
object EmojiTokenScanner {

    private fun isNameChar(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || c == '+' || c == '-'

    /**
     * @param text 全体テキスト
     * @param cursor カーソル位置（0..text.length）
     * @return 検出したトークン。トークンが無い場合は null。
     */
    fun findCurrentToken(text: String, cursor: Int): EmojiToken? {
        if (cursor < 0 || cursor > text.length) return null

        // カーソルから後方へ、絵文字名として有効な文字が続く限り遡る
        var i = cursor
        while (i > 0 && isNameChar(text[i - 1])) {
            i--
        }

        // 名前文字列の直前が `:` でなければトークンではない
        val colonIndex = i - 1
        if (colonIndex < 0 || text[colonIndex] != ':') return null

        val query = text.substring(i, cursor)
        return EmojiToken(start = colonIndex, end = cursor, query = query)
    }
}
