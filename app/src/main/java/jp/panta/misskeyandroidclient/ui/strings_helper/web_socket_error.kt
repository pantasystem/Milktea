package jp.panta.misskeyandroidclient.ui.strings_helper

import android.content.Context
import android.widget.Toast
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.api_streaming.Socket


fun Context.webSocketStateMessageScope(block: WebSocketStateMessageScope.()->Unit) {
    block.invoke(WebSocketStateMessageScope(this))
}

class WebSocketStateMessageScope(val context: Context) {
    fun Socket.State.showToastMessage() {
        val message = getStateMessage()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun Socket.State.getStateMessage(): String {
        return  when(this){
            is Socket.State.Connected -> context.getString(R.string.connected)
            is Socket.State.Connecting -> if (this.isReconnect) {
                context.getString(R.string.connecting)
            } else {
                context.getString(R.string.connecting)
            }
            is Socket.State.Closing -> context.getString(R.string.closing)
            is Socket.State.Failure -> context.getString(R.string.websocket_error) + this.throwable
            is Socket.State.Closed -> context.getString(R.string.closed)
            is Socket.State.NeverConnected -> ""
        }
    }
}




