package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*
import kotlin.collections.ArrayList

class MainCapture(
    val connectionInstance: ConnectionInstance,
    val gson: Gson
) : Observer{
    interface Listener{
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
    }

    private data class Channel<T>(val type: String, val body: Body<T>)
    private data class Body<T>(val id: String,val type: String, val body: T)

    private data class Request(val type: String = "connect", val body: Body){
        data class Body(val channel: String = "main", val id: String)
    }

    override var streamingAdapter: StreamingAdapter? = null

    private val listeners = ArrayList<Listener>()

    private val notificationType = TypeToken.getParameterized(Channel::class.java, Notification::class.java).type
    private val messageType = TypeToken.getParameterized(Channel::class.java, Message::class.java).type
    private val noteType = TypeToken.getParameterized(Channel::class.java, Note::class.java).type
    private val userType = TypeToken.getParameterized(Channel::class.java, User::class.java).type
    private val stringType = TypeToken.getParameterized(Channel::class.java, String::class.java).type
    private val fileType = TypeToken.getParameterized(Channel::class.java, FileProperty::class.java).type

    private var mId: String = UUID.randomUUID().toString()

    override fun onConnect() {
        mId = UUID.randomUUID().toString()
        val request = Request(body = Request.Body(id = mId))
        streamingAdapter?.send(gson.toJson(request))
    }

    override fun onDissconnect() {

    }

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
            listeners.forEach{
                when(type){
                    "notification" ->{
                        //notification
                        notifyNotification(msg, it::notification)
                    }
                    "readAllNotifications" ->{
                        //null
                        it.readAllNotifications()
                    }

                    "unreadMessagingMessage" ->{
                        //message
                        notifyMessage(msg, it::unreadMessagingMessage)
                    }
                    "mention" ->{
                        //note
                        notifyNote(msg, it::mention)
                    }
                    "unreadMention" ->{
                        //id
                        val data: Channel<String> = gson.fromJson(msg, stringType)
                        it.unreadMention(data.body.body)
                    }
                    "renote" ->{
                        //note
                        notifyNote(msg, it::renote)
                    }
                    "messagingMessage" ->{
                        //message
                        notifyMessage(msg, it::messagingMessage)
                    }
                    "meUpdated" ->{
                        //user
                        notifyUser(msg, it::meUpdated)
                    }
                    "unfollow" ->{
                        //user
                        notifyUser(msg, it::unFollowed)
                    }
                    "follow"->{
                        notifyUser(msg, it::follow)
                    }
                    "followed" ->{
                        //user
                        notifyUser(msg, it::followed)
                    }
                    "fileUpdated" ->{
                        notifyFile(msg, it::fileUpdated)
                    }
                    "driveFileCreated" ->{
                        notifyFile(msg, it::fileCreated)
                    }
                    "fileDeleted" ->{
                        notifyId(msg, it::fileDeleted)
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

    fun addListener(listener: Listener){
        listeners.add(listener)
    }

    fun clearListener(){
        listeners.clear()
    }
}