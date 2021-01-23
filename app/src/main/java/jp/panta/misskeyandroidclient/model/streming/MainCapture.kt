package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.api.notes.Note
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.api.users.User
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * アカウント一つにつきMainCaptureを一つにしたい
 */
class MainCapture(
    override val account: Account,
    val gson: Gson
) : AbsObserver(){
    interface Listener{
        val id: String
        fun notification(notification: Notification)
        fun readAllNotifications()
        fun unreadMessagingMessage(message: Message)
        fun mention(note: Note)
        fun unreadMention(id: String)
        fun renote(note: Note)
        fun messagingMessage(message: Message)
        fun meUpdated(user: User)

        /**
         * フォロー解除したときに呼び出される
         */
        fun unFollowed(user: User)

        /**
         * ユーザーにフォローされたときに呼び出される
         */
        fun followed(user: User)

        /**
         * ユーザーをフォローしたときに呼び出される
         */
        fun follow(user: User)

        fun fileCreated(fileProperty: FileProperty)
        fun fileDeleted(id: String)
        fun fileUpdated(fileProperty: FileProperty)
    }

    abstract class AbsListener : Listener{
        override val id: String = UUID.randomUUID().toString()

        override fun followed(user: User) {}
        override fun notification(notification: Notification) {}
        override fun meUpdated(user: User) {}
        override fun mention(note: Note) {}
        override fun messagingMessage(message: Message) {}
        override fun readAllNotifications() {}
        override fun renote(note: Note) {}
        override fun unFollowed(user: User) {}
        override fun unreadMention(id: String) {}
        override fun unreadMessagingMessage(message: Message) = Unit
        override fun follow(user: User) = Unit
        override fun fileDeleted(id: String) = Unit
        override fun fileUpdated(fileProperty: FileProperty) = Unit
        override fun fileCreated(fileProperty: FileProperty) = Unit

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AbsListener

            if (id != other.id) return false

            return true
        }


    }

    class Factory(val gson: Gson, val setUpFunction: (Account, Observer)-> Unit){

        private val accountAndMainCapture = ConcurrentHashMap<Long, MainCapture>()

        fun create(account: Account) : MainCapture{
            synchronized(this){
                var mainCapture = accountAndMainCapture[account.accountId]
                if(mainCapture == null) {

                    mainCapture = MainCapture(account, gson)
                    accountAndMainCapture[account.accountId] = mainCapture
                    setUpFunction.invoke(account, mainCapture)
                }
                return mainCapture
            }
        }
    }

    private data class Channel<T>(val type: String, val body: Body<T>)
    private data class Body<T>(val id: String,val type: String, val body: T)

    private data class Request(val type: String = "connect", val body: Body){
        data class Body(val channel: String = "main", val id: String)
    }

    override var streamingAdapter: StreamingAdapter? = null

    private val mListeners = WeakHashMap<String, Listener>()

    private val notificationType = TypeToken.getParameterized(Channel::class.java, Notification::class.java).type
    private val messageType = TypeToken.getParameterized(Channel::class.java, Message::class.java).type
    private val noteType = TypeToken.getParameterized(Channel::class.java, Note::class.java).type
    private val userType = TypeToken.getParameterized(Channel::class.java, User::class.java).type
    private val stringType = TypeToken.getParameterized(Channel::class.java, String::class.java).type
    private val fileType = TypeToken.getParameterized(Channel::class.java, FileProperty::class.java).type

    private var mId: String = UUID.randomUUID().toString()

    override fun onConnect() {
        // 接続する毎にIDを再生成する
        Log.d("MainCapture", "接続を開始します")
        mId = UUID.randomUUID().toString()
        val request = Request(body = Request.Body(id = mId))
        if(streamingAdapter == null){
            Log.d("MainCapture", "streamingAdapter is nullなので送信できない")
        }else{
            if(streamingAdapter?.isConnect == true){
                streamingAdapter?.send(gson.toJson(request))
            }else{
                Log.d("MainCapture", "streamingAdapter未接続のため送信できない")
            }
        }
    }

    override fun onClosing() {
        val closeRequest = Request("disconnect", Request.Body(id = mId))
        streamingAdapter?.send(gson.toJson(closeRequest))
    }

    override fun onDisconnect() = Unit

    override fun onReceived(msg: String) {
        try{

            val jsonType = object : TypeToken<Map<String, Any?>>(){}.type
            val tmpJson  = gson.fromJson<Map<String, Any?>>(msg, jsonType)
            val body = (tmpJson["body"] as Map<*, *>)
            val type = body["type"] as String
            val id = body["id"] as String

            if(id != mId){
                return
            }
            //Log.d("MainCapture", msg)
            synchronized(mListeners){
                mListeners.forEach{
                    val listener = it.value
                    when(type){
                        "notification" ->{
                            //notification
                            notifyNotification(msg, listener::notification)
                        }
                        "readAllNotifications" ->{
                            //null
                            listener.readAllNotifications()
                        }

                        "unreadMessagingMessage" ->{
                            //message
                            notifyMessage(msg, listener::unreadMessagingMessage)
                        }
                        "mention" ->{
                            //note
                            notifyNote(msg, listener::mention)
                        }
                        "unreadMention" ->{
                            //id
                            val data: Channel<String> = gson.fromJson(msg, stringType)
                            listener.unreadMention(data.body.body)
                        }
                        "renote" ->{
                            //note
                            notifyNote(msg, listener::renote)
                        }
                        "messagingMessage" ->{
                            //message
                            notifyMessage(msg, listener::messagingMessage)
                        }
                        "meUpdated" ->{
                            //user
                            notifyUser(msg, listener::meUpdated)
                        }
                        "unfollow" ->{
                            //user
                            notifyUser(msg, listener::unFollowed)
                        }
                        "follow"->{
                            notifyUser(msg, listener::follow)
                        }
                        "followed" ->{
                            //user
                            notifyUser(msg, listener::followed)
                        }
                        "fileUpdated" ->{
                            notifyFile(msg, listener::fileUpdated)
                        }
                        "driveFileCreated" ->{
                            notifyFile(msg, listener::fileCreated)
                        }
                        "fileDeleted" ->{
                            notifyId(msg, listener::fileDeleted)
                        }

                    }

                }
            }


        }catch(e: Exception){
            Log.e("MainCapture", "error", e)
        }
    }

    private fun notifyNotification(msg: String, observer: (Notification)-> Unit){
        val notification: Channel<Notification> = gson.fromJson(msg, notificationType)
        observer(notification.body.body)
    }

    private fun notifyUser(msg: String, observer: (User)-> Unit){
        val user: Channel<User> = gson.fromJson(msg, userType)
        observer(user.body.body)
    }

    private fun notifyNote(msg: String, observer: (Note)-> Unit){
        val note: Channel<Note> = gson.fromJson(msg, noteType)
        observer(note.body.body)
    }

    private fun notifyMessage(msg: String, observer: (Message) -> Unit){
        val message: Channel<Message> = gson.fromJson(msg, messageType)
        observer(message.body.body)
    }

    private fun notifyId(msg: String, observer: (String)-> Unit){
        val obj: Channel<String> = gson.fromJson(msg, stringType)
        val id = obj.body.body
        observer(id)
    }

    private fun notifyFile(msg: String, observer: (FileProperty)-> Unit){
        val fileProperty: Channel<FileProperty> = gson.fromJson(msg, fileType)
        observer(fileProperty.body.body)
    }

    fun putListener(listener: Listener){
        synchronized(mListeners){
            mListeners[listener.id] = listener
        }
    }

    fun removeListener(listener: Listener){
        synchronized(mListeners){
            mListeners.remove(listener.id)
        }
    }

    fun clearListener(){
        synchronized(mListeners){
            mListeners.clear()
        }
    }
}