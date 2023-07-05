package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType

/**
 * タグの判別ロジック、正規表現、判別用データなどをここに実装する
 */
object MFMContract {

    val blockTypeTagNameMap = mapOf(
        "i" to ElementType.ITALIC,
        "small" to ElementType.SMALL,
        "center" to ElementType.CENTER
        /*"flip" to TagType.FLIP_HORIZONTAL,
        "spin" to TagType.SPIN,
        "motion" to TagType.MOTION,
        "jump" to TagType.JUMP*/
    )

    val fnTypeTagNameMap = mapOf(
        "x2" to ElementType.FnX2,
        "x3" to ElementType.FnX3,
        "x4" to ElementType.FnX4
    )
}