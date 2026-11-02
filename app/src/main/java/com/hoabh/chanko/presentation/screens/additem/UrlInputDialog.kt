package com.hoabh.chanko.presentation.screens.additem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.AddItemStep
import com.hoabh.chanko.ui.theme.CandyColors

private val Ink = CandyColors.UrlImport.ink
private val Purple = CandyColors.UrlImport.purple
private val Outline = CandyColors.UrlImport.outline

/** Main stays visible under this overlay. State belongs only to Main's in-memory ViewModel. */
@Composable
fun AddItemEntryHost(viewModel: AddItemViewModel, onEffect: (AddItemEffect) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectAddItemEffects(viewModel, onEffect)
    if (state.active && state.step == AddItemStep.URL_DIALOG) {
        @Suppress("DEPRECATION")
        val clipboardManager = LocalClipboardManager.current
        if (state.extractionFailed) {
            ExtractionFailedDialog(
                onCancel = viewModel::dismissExtractionFailure,
                onConfirm = viewModel::confirmManualInput
            )
        } else {
            UrlInputDialog(
                url = state.form.url,
                extracting = state.extracting,
                onUrlChanged = viewModel::editUrl,
                onPaste = {
                    val clip = clipboardManager.getText()?.text
                    if (!clip.isNullOrBlank()) {
                        viewModel.editUrl(clip.trim())
                    }
                },
                onExtract = viewModel::extract,
                onCancel = viewModel::cancel
            )
        }
    }
}

@Composable
internal fun CollectAddItemEffects(viewModel: AddItemViewModel, onEffect: (AddItemEffect) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val handler by rememberUpdatedState(onEffect)
    LaunchedEffect(viewModel, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { handler(it) }
        }
    }
}

@Composable
internal fun CandyModal(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
internal fun CandyCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = CandyColors.UrlImport.dialogSurface,
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun UrlInputDialog(
    url: String,
    extracting: Boolean,
    urlError: String? = null,
    onUrlChanged: (String) -> Unit,
    onPaste: () -> Unit = {},
    onExtract: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loading = stringResource(R.string.candy_extracting)
    CandyModal(onDismiss = onCancel) {
        Box(modifier = modifier.fillMaxWidth()) {
            Box(Modifier.padding(top = 80.dp)) {
                CandyCard {
                    Spacer(Modifier.height(12.dp))
                    CandyHeading(stringResource(R.string.candy_url_title))
                    Text(
                        text = stringResource(R.string.candy_url_body),
                        color = CandyColors.UrlImport.mutedText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp),
                        enabled = !extracting,
                        singleLine = true,
                        label = { Text(stringResource(R.string.candy_url_label)) },
                        placeholder = { Text(stringResource(R.string.candy_url_hint)) },
                        isError = urlError != null,
                        supportingText = if (urlError != null) { { Text(urlError) } } else null,
                        shape = RoundedCornerShape(20.dp),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.chanko_dialog_link),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = onPaste,
                                enabled = !extracting,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.chanko_dialog_paste),
                                    contentDescription = stringResource(R.string.candy_url_paste),
                                    tint = Purple
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!extracting && url.isNotBlank()) onExtract()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = CandyColors.UrlImport.fieldContainer,
                            unfocusedContainerColor = CandyColors.UrlImport.fieldContainer
                        )
                    )
                    if (extracting) {
                        Text(
                            text = loading,
                            style = MaterialTheme.typography.bodySmall,
                            color = Purple,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    CandyActions(
                        label = stringResource(R.string.candy_extract),
                        loading = extracting,
                        enabled = !extracting && url.isNotBlank(),
                        onPrimary = onExtract,
                        onCancel = onCancel
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.chanko_url_basket),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 160.dp, height = 112.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ExtractionFailedDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    CandyModal(onDismiss = onCancel) {
        Box(modifier = modifier.fillMaxWidth()) {
            CandyCard {
                Icon(
                    painter = painterResource(R.drawable.chanko_dialog_warning),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
                CandyHeading(stringResource(R.string.candy_failure_title))
                Text(
                    text = stringResource(R.string.candy_failure_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CandyColors.UrlImport.mutedText,
                    textAlign = TextAlign.Center
                )
                CandyActions(
                    label = stringResource(R.string.candy_confirm),
                    loading = false,
                    enabled = true,
                    onPrimary = onConfirm,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun CandyHeading(text: String) {
    Text(
        text = text,
        color = Ink,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun CandyActions(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    onPrimary: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CandyCancel(
            onClick = onCancel,
            modifier = Modifier.weight(0.4f)
        )
        CandyPrimary(
            label = label,
            loading = loading,
            enabled = enabled,
            onClick = onPrimary,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun CandyCancel(onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Outline),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CandyColors.UrlImport.disabledButtonBg,
            contentColor = Purple
        )
    ) {
        Text(stringResource(R.string.candy_cancel), fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun CandyPrimary(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    val brush = Brush.verticalGradient(
        if (enabled || loading)
            listOf(CandyColors.UrlImport.submitGradientStart, CandyColors.UrlImport.submitGradientEnd)
        else
            listOf(CandyColors.UrlImport.disabledGradient, CandyColors.UrlImport.disabledGradient)
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 52.dp)
            .shadow(if (enabled) 4.dp else 0.dp, shape)
            .clip(shape)
            .background(brush),
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = if (loading) Color.White else CandyColors.UrlImport.disabledButtonText
        )
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(label, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1)
        if (!loading) {
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.chanko_dialog_arrow),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
    }
}
