package com.hoabh.chanko.presentation.features.additem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.presentation.features.additem.components.*
import com.hoabh.chanko.ui.theme.ChankoTheme

/** Exact illustrative content from DESIGN-REVIEW.html. Never bound into runtime evaluation. */
private fun sampleForm() = AddItemFormState(
    url = "https://shop.example/tai-nghe", title = "Tai nghe không dây", rawPrice = "2.490.000", category = "Điện tử",
    reason = "Tai nghe cũ đã hỏng, tôi cần dùng khi làm việc.", confirmedPrice = 2490000, saveIdempotencyKey = "preview-only")

@Composable
private fun ScreenPreview(state: AddItemUiState, step: AddItemStep) {
    ChankoTheme { AddItemFlowScreen(state, step, {}, {}, {}, {}, {}, { _, _ -> }, {}) }
}
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "01 · Ready")
@Preview(showBackground = true, locale = "vi", widthDp = 320, heightDp = 568, fontScale = 2f, name = "01 · Font 200%")
@Preview(showBackground = true, locale = "vi", widthDp = 640, heightDp = 360, name = "01 · Landscape")
@Composable private fun FormPreview() = ScreenPreview(AddItemUiState(active = true, form = sampleForm()), AddItemStep.MANUAL_INPUT)

@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "01 · Empty")
@Composable private fun EmptyFormPreview() = ScreenPreview(AddItemUiState(
    active = true, form = AddItemFormState(saveIdempotencyKey = "preview")), AddItemStep.MANUAL_INPUT)

@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "02 · Question fixture")
@Composable
private fun QuickCheckPreview() = ScreenPreview(AddItemUiState(active = true, form = sampleForm(),
    evaluation = EvaluationState.Question(QuickCheckQuestion("sample", "Bạn cần dùng món này trong thời gian gần không?",
        listOf("Cần dùng ngay", "Có thể chờ thêm", "Tôi chưa chắc")), 0)), AddItemStep.QUICK_CHECK)

@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "02 · Service unavailable")
@Composable private fun UnavailablePreview() = ScreenPreview(AddItemUiState(active = true, form = sampleForm()), AddItemStep.QUICK_CHECK)

@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "03 · BUY fixture")
@Composable private fun BuyPreview() = ResultPreview(Verdict.BUY)
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "03 · WAIT fixture")
@Composable private fun WaitPreview() = ResultPreview(Verdict.WAIT)
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "03 · SKIP fixture")
@Composable private fun SkipPreview() = ResultPreview(Verdict.SKIP)
@Composable
private fun ResultPreview(verdict: Verdict) {
    val reason = when (verdict) {
        Verdict.BUY -> "Bạn đã nêu nhu cầu sử dụng rõ ràng. Việc mua có thể phù hợp nếu mức giá đã xác nhận đáp ứng khả năng chi trả của bạn."
        Verdict.WAIT -> "Dành thêm thời gian để xem nhu cầu này có còn quan trọng với bạn."
        Verdict.SKIP -> "Hãy xem lại nhu cầu sử dụng trước khi quyết định."
    }
    ScreenPreview(AddItemUiState(active = true, form = sampleForm().copy(assessment = AssessmentSnapshot(verdict,
        listOf(reason), listOf("Nội dung minh họa, không phải đánh giá tài chính thật."), 0, 0, "preview-only"))), AddItemStep.DECISION_RESULT)
}

@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "URL dialog")
@Composable private fun UrlDialogPreview() {
    ChankoTheme { UrlInputDialog("https://shop.example/tai-nghe", false, onUrlChanged = {}, onExtract = {}, onCancel = {}) }
}
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "URL dialog · Extracting")
@Composable private fun UrlDialogExtractingPreview() {
    ChankoTheme { UrlInputDialog("https://shop.example/tai-nghe", true, onUrlChanged = {}, onExtract = {}, onCancel = {}) }
}
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "Extraction failed")
@Composable private fun ExtractionFailedPreview() {
    ChankoTheme { ExtractionFailedDialog({}, {}) }
}
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "Verify partial extraction")
@Composable private fun VerifyPreview() = ScreenPreview(AddItemUiState(active = true,
    form = AddItemFormState(title = "Tai nghe không dây")), AddItemStep.VERIFY_INFO)
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "Duplicate sheet")
@Composable private fun DuplicatePreview() {
    ChankoTheme { DuplicateWarningSheet(DuplicateItemSummary("preview", "Tai nghe không dây", 2490000), {}, {}, {}) }
}
@Preview(showBackground = true, locale = "vi", widthDp = 393, heightDp = 852, name = "Variant sheet")
@Composable private fun VariantPreview() {
    ChankoTheme { VariantPickerSheet(listOf(ProductVariantOption("black", "Màu đen", 2490000),
        ProductVariantOption("silver", "Màu bạc", 2690000)), {}, {}) }
}
