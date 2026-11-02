package com.hoabh.chanko.presentation.screens.additem

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.AddItemFormState
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun VerifyItemInfoScreen(form: AddItemFormState, enabled: Boolean,
    onEdit: ((AddItemFormState) -> AddItemFormState) -> Unit, onPhotoError: () -> Unit) {
    HelpText(stringResource(R.string.add_item_verify_help), Modifier.padding(bottom = CandySpace.lg))
    AddItemFormScreen(form, enabled, onEdit, onPhotoError)
}
