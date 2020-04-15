package jp.panta.misskeyandroidclient.mfm

enum class TagType(val tagClass: TagClass) {
    QUOTE(TagClass.QUOTE),
    BOLD(TagClass.STANDARD),
    ITALIC(TagClass.STANDARD),
    CENTER(TagClass.STANDARD),
    STRIKE(TagClass.STANDARD),
    LATERAL_EXPANSION_AND_CONTRACTION(TagClass.ANIMATION),
    LATERAL_EXPANSION_AND_CONTRACTION_SYMMETRICAL_SHAKING(TagClass.ANIMATION),
    FLIP_HORIZONTAL(TagClass.ANIMATION),
    SPIN(TagClass.ANIMATION),
    JUMP(TagClass.ANIMATION),
    CODE(TagClass.CODE),
    SEARCH(TagClass.SEARCH),
    TITLE(TagClass.TITLE)
}

enum class TagClass(val weight: Int){
    STANDARD(3),
    QUOTE(3),
    ANIMATION(2),
    CODE(0),
    SEARCH(0),
    TITLE(1),
}