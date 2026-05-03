package ca.arzook.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@Composable
fun rememberFilePicker(onFilePicked: (ByteArray, String) -> Unit) = run {
    val scope = rememberCoroutineScope()
    rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("jpg", "jpeg", "png", "pdf")),
        mode = PickerMode.Single
    ) { file ->
        if (file != null) {
            scope.launch {
                val bytes = file.readBytes()
                onFilePicked(bytes, file.name)
            }
        }
    }
}
