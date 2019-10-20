package jp.panta.misskeyandroidclient.model.streming

class StreamingAdapter{

    val observers = ArrayList<Observer>()

    fun addObserver(observer: Observer){
        observer.onConnect()
    }

    fun send(json: String){

    }
}