package jp.panta.misskeyandroidclient.mfm

object MFMParser{

    /**
     * タグの先頭の文字
     * その”タグ”であるかを判定するための関数を持つ
     */
    val beginningOfHighLevelTag = mapOf(
        '*' to { text: String ->
            true
        },
        '<' to { text: String ->
            true
        },
        '<' to { text: String ->
            true
        },
        '<' to { text ->
            true
        }

    )
}