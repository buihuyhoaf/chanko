package com.hoabh.chanko.ui.mascot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.ui.theme.ChankoTheme

class MascotStatePreviewProvider : PreviewParameterProvider<MascotPresentationState> {
    override val values = MascotPresentationState.entries.asSequence()
}

@Preview(name = "Mascot slots — no approved assets", showBackground = true, widthDp = 320)
@Preview(name = "Mascot slots — 200%", showBackground = true, widthDp = 320, fontScale = 2f)
@Composable
private fun MascotPreview(@PreviewParameter(MascotStatePreviewProvider::class) state: MascotPresentationState) {
    ChankoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(state.name)
            Text("Placeholder: awaiting approved transparent asset")
            ChankoMascot(state)
        }
    }
}
