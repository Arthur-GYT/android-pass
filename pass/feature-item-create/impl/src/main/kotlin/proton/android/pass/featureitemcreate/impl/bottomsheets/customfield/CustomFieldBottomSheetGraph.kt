package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.bottomSheet

object CustomFieldIndexNavArgId : NavArgId {
    override val key = "index"
    override val navType = NavType.IntType
}

object CustomFieldTitleNavArgId : NavArgId {
    override val key = "title"
    override val navType = NavType.StringType
}

object AddCustomFieldBottomSheet : NavItem("item/create/customfield/add/bottomsheet")
object CustomFieldOptionsBottomSheet : NavItem(
    baseRoute = "item/create/customfield/options/bottomsheet",
    navArgIds = listOf(CustomFieldIndexNavArgId, CustomFieldTitleNavArgId)
) {
    fun buildRoute(index: Int, currentTitle: String) =
        "$baseRoute/$index/${NavParamEncoder.encode(currentTitle)}"
}

enum class CustomFieldType {
    Text,
    Hidden,
    Totp
}

sealed interface AddCustomFieldNavigation {
    object Close : AddCustomFieldNavigation
    object AddText : AddCustomFieldNavigation
    object AddHidden : AddCustomFieldNavigation
    object AddTotp : AddCustomFieldNavigation
}

sealed interface CustomFieldOptionsNavigation {
    object Close : CustomFieldOptionsNavigation
    data class EditCustomField(val index: Int, val title: String) : CustomFieldOptionsNavigation
    object RemoveCustomField : CustomFieldOptionsNavigation
}

fun NavGraphBuilder.customFieldBottomSheetGraph(
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    bottomSheet(AddCustomFieldBottomSheet) {
        AddCustomFieldBottomSheet {
            when (it) {
                is AddCustomFieldNavigation.Close -> {
                    onNavigate(BaseLoginNavigation.Close)
                }
                is AddCustomFieldNavigation.AddText -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Text))
                }
                is AddCustomFieldNavigation.AddHidden -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Hidden))
                }
                is AddCustomFieldNavigation.AddTotp -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Totp))
                }
            }
        }
    }

    bottomSheet(CustomFieldOptionsBottomSheet) {
        EditCustomFieldBottomSheet(
            onNavigate = {
                when (it) {
                    is CustomFieldOptionsNavigation.EditCustomField -> {
                        onNavigate(BaseLoginNavigation.EditCustomField(it.title, it.index))
                    }
                    CustomFieldOptionsNavigation.RemoveCustomField -> {
                        onNavigate(BaseLoginNavigation.RemovedCustomField)
                    }
                    CustomFieldOptionsNavigation.Close -> {
                        onNavigate(BaseLoginNavigation.Close)
                    }
                }
            }
        )
    }
}