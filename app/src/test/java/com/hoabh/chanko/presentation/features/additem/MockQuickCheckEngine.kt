package com.hoabh.chanko.presentation.features.additem

import com.hoabh.chanko.domain.additem.*

/** Chooses an explicit fixture, never scores the user's financial information. */
internal class MockQuickCheckEngine(private val fixture: EvaluationState) : QuickCheckEvaluationContract {
    override suspend fun evaluate(form: AddItemFormState): EvaluationState = fixture
}
