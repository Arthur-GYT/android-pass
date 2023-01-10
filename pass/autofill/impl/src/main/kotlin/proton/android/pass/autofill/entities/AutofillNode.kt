package proton.android.pass.autofill.entities

import android.text.InputType
import android.view.autofill.AutofillValue
import proton.android.pass.common.api.Option

@JvmInline
value class InputTypeValue(val value: Int) {

    fun hasVariations(vararg variations: Int): Boolean =
        variations.any { this.value and InputType.TYPE_MASK_VARIATION == it }
}

data class AutofillNode(
    val id: AutofillFieldId?,
    val className: String?,
    val isImportantForAutofill: Boolean,
    val text: String?,
    val autofillValue: AutofillValue?,
    val inputType: InputTypeValue,
    val autofillHints: List<String>,
    val htmlAttributes: List<Pair<String, String>>,
    val children: List<AutofillNode>,
    val url: Option<String>,
    val hintKeywordList: List<CharSequence>
)
