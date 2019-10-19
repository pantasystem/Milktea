package jp.panta.misskeyandroidclient.viewmodel.notes.reaction

import androidx.lifecycle.MutableLiveData

//DDDでいうVOに属する
class Reaction(reaction: Pair<String, Int>){
    val reactionCount: Int = reaction.second
    val reaction: String = reaction.first
    //val reactionString: String
}