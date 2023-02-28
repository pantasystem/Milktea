package net.pantasystem.milktea.common_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <T> Spinner(
    modifier: Modifier = Modifier,
    dropDownModifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    selectedItemFactory: @Composable (Modifier, T) -> Unit,
    dropdownItemFactory: @Composable (T, Int) -> Unit,
) {
    var expanded: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        selectedItemFactory(
            Modifier
                .clickable { expanded = true },
            selectedItem
        )

        androidx.compose.material.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = dropDownModifier
        ) {
            items.forEachIndexed { index, element ->
                DropdownMenuItem(onClick = {
                    onItemSelected(items[index])
                    expanded = false
                }) {
                    dropdownItemFactory(element, index)
                }
            }
        }
    }
}