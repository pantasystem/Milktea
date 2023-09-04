package net.pantasystem.milktea.common_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

@Composable
fun <T : Fragment> rememberFragment(
    vararg inputs: Any?,
    fragmentManager: FragmentManager,
    initial: () -> T,
): T {
    return rememberSaveable(
        inputs = inputs,
        saver = Saver(
            save = { fragment ->
                if (fragment in fragmentManager.fragments) {
                    fragmentManager
                        .saveFragmentInstanceState(fragment)
                } else {
                    null
                }
            },
            restore = { savedState ->
                initial().also { fragment ->
                    fragment.setInitialSavedState(savedState)
                }
            },
        ),
    ) {
        initial()
    }
}