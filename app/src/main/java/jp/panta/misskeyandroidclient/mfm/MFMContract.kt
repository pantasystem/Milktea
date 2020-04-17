package jp.panta.misskeyandroidclient.mfm

/**
 * タグの判別ロジック、正規表現、判別用データなどをここに実装する
 */
object MFMContract {

    val blockTypeTagNameMap = mapOf(
        "i" to TagType.ITALIC,
        "small" to TagType.SMALL,
        "center" to TagType.CENTER
        /*"flip" to TagType.FLIP_HORIZONTAL,
        "spin" to TagType.SPIN,
        "motion" to TagType.MOTION,
        "jump" to TagType.JUMP*/
    )
}