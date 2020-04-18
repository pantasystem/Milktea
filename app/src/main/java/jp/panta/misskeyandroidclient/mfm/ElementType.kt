package jp.panta.misskeyandroidclient.mfm

enum class ElementType(val elementClass: ElementClass) {
    QUOTE(ElementClass.QUOTE),
    BOLD(ElementClass.STANDARD),
    ITALIC(ElementClass.STANDARD),
    CENTER(ElementClass.STANDARD),
    STRIKE(ElementClass.STANDARD),
    LATERAL_EXPANSION_AND_CONTRACTION(ElementClass.ANIMATION),
    LATERAL_EXPANSION_AND_CONTRACTION_SYMMETRICAL_SHAKING(ElementClass.ANIMATION),
    FLIP_HORIZONTAL(ElementClass.ANIMATION),
    SPIN(ElementClass.ANIMATION),
    JUMP(ElementClass.ANIMATION),
    CODE(ElementClass.CODE),
    SEARCH(ElementClass.SEARCH),
    TITLE(ElementClass.TITLE),
    ROOT(ElementClass.STANDARD),
    MOTION(ElementClass.ANIMATION),
    SMALL(ElementClass.STANDARD),
    LINK(ElementClass.LINK),
    TEXT(ElementClass.TEXT),
    EMOJI(ElementClass.EMOJI)
}

enum class ElementClass(val weight: Int){
    STANDARD(3),
    QUOTE(4),
    ANIMATION(2),
    CODE(0),
    SEARCH(0),
    TITLE(1),
    LINK(0),
    TEXT(0),
    EMOJI(0)
}