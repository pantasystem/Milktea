package net.pantasystem.milktea.note.renote

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RenoteResultHandler(
    val viewModel: RenoteViewModel,
    val scope: CoroutineScope,
    val lifecycle: Lifecycle,
    val context: Context,
) {

    fun setup() {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.resultEvents.collect { event ->
                    handle(event)
                }
            }
        }
    }

    private fun handle(event: RenoteActionResultEvent) {
        when (event) {
            is RenoteActionResultEvent.Renote -> {
                event.result.onFailure {
                    Toast.makeText(context, "Failed Renote", Toast.LENGTH_LONG).show()
                }.onSuccess {
                    val failedCount = it.count { nr ->
                        nr.isFailure
                    }
                    val successCount = it.count { nr ->
                        nr.isSuccess
                    }
                    Toast.makeText(
                        context,
                        "Success Renote($successCount)/ Failed Renote($failedCount)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            is RenoteActionResultEvent.UnRenote -> {
                event.result.onSuccess {
                    Toast.makeText(
                        context,
                        "Successful delete Renote",
                        Toast.LENGTH_LONG
                    ).show()
                }.onFailure {
                    Toast.makeText(context, "Failed delete Renote", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }
    }
}