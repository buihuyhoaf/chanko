package com.hoabh.chanko.domain.additem

import java.net.URI

enum class AddItemStep { URL_DIALOG, VERIFY_INFO, MANUAL_INPUT, QUICK_CHECK, DECISION_RESULT }
enum class Verdict { BUY, WAIT, SKIP }
enum class CoolingChoice { UNSELECTED, APPLIED, NOT_APPLIED }
data class AssessmentSnapshot(
    val verdict: Verdict, val reasons: List<String>, val limitations: List<String>,
    val productRevision: Long, val answerRevision: Long, val engineVersion: String
)
data class AddItemFormState(
    val url: String = "",
    val title: String = "",
    val rawPrice: String = "",
    val category: String = "",
    val reason: String = "",
    val photoUri: String = "",
    val confirmedPrice: Long? = null,
    val productRevision: Long = 0,
    val answerRevision: Long = 0,
    val questionIds: List<String> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val branchVersion: String = "",
    val questionIndex: Int = 0,
    val earlyAssessment: Boolean = false,
    val assessment: AssessmentSnapshot? = null,
    val coolingChoice: CoolingChoice = CoolingChoice.UNSELECTED,
    val coolingDurationMillis: Long? = null,
    val saveIdempotencyKey: String = ""
) {
    val price: Long? get() = parseExactVnd(rawPrice)
    val canContinue: Boolean get() = title.isNotBlank() && category.isNotBlank() &&
        reason.isNotBlank() && price != null && confirmedPrice == price
    val hasContent: Boolean get() = listOf(url, title, rawPrice, category, reason, photoUri).any { it.isNotBlank() }
    fun editProduct(transform: (AddItemFormState) -> AddItemFormState): AddItemFormState {
        val updated = transform(this)
        return updated.copy(productRevision = productRevision + 1, confirmedPrice = null,
            assessment = null, answers = emptyMap(), questionIds = emptyList(),
            answerRevision = answerRevision + 1, questionIndex = 0,
            coolingChoice = CoolingChoice.UNSELECTED, coolingDurationMillis = null,
            earlyAssessment = false, branchVersion = "")
    }
}

/** Accept only unambiguous integer VND, including strictly grouped Vietnamese thousands. */
fun parseExactVnd(raw: String): Long? {
    val text = raw.trim()
    if (!Regex("[0-9]+|[1-9][0-9]{0,2}(\\.[0-9]{3})+").matches(text)) return null
    return text.replace(".", "").toLongOrNull()?.takeIf { it > 0 }
}
fun isProductUrl(raw: String): Boolean = try {
    val uri = URI(raw.trim())
    uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank() && uri.userInfo == null
} catch (_: Exception) { false }

data class QuickCheckQuestion(val id: String, val text: String, val choices: List<String>)
sealed interface EvaluationState {
    data object Unavailable : EvaluationState
    data class Question(val question: QuickCheckQuestion, val index: Int) : EvaluationState
    data class Result(val snapshot: AssessmentSnapshot) : EvaluationState
}
/** Contract sketch only; no financial scoring or network contract is invented here. */
fun interface QuickCheckEvaluationContract {
    suspend fun evaluate(form: AddItemFormState): EvaluationState
}
class UnavailableQuickCheckEngine : QuickCheckEvaluationContract {
    override suspend fun evaluate(form: AddItemFormState) = EvaluationState.Unavailable
}

/** Local integration seam, not a claimed backend DTO. Partial success leaves missing fields empty. */
data class ExtractedItemInfo(val title: String = "", val priceVnd: Long? = null,
    val category: String = "", val photoUri: String = "")
sealed interface ExtractionResult {
    data class Success(val item: ExtractedItemInfo) : ExtractionResult
    data object Failed : ExtractionResult
}
fun interface UrlExtractionService {
    suspend fun extract(url: String): ExtractionResult
}
class UnavailableUrlExtractionService : UrlExtractionService {
    override suspend fun extract(url: String) = ExtractionResult.Failed
}
