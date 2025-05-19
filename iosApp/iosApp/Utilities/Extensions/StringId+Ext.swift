import SwiftUI
import shared

extension StringId {
    var localized: String {
        let resId: String.LocalizationValue = switch self {
        case .petTypeDescriptionCatus: "pet_type_description_catus"
        case .petTypeDescriptionDogus: "pet_type_description_dogus"
        case .petTypeDescriptionFrogus: "pet_type_description_frogus"
        case .whatIsGoingOnWithYou: "what_is_going_on_with_you"
        case .beingBetter: "being_better"
        case .byeBye: "bye_bye"
        case .ishouldGo: "i_should_go"
        case .seeYa: "see_ya"
        case .sure: "Sure"
        case .ok: "Ok"
        case .thanks: "Thanks"
        case .use: "Use"
        case .destroy: "Destroy"
        case .necronomiconStage3AnswerOption0: "NecronomiconStage3AnswerOption0"
        case .necronomiconStage3CommonDialog: "NecronomiconStage3CommonDialog"
        case .necronomiconStage3DogDialog: "NecronomiconStage3DogDialog"
        case .necronomiconStage3DogDialogAnswerOption0: "NecronomiconStage3DogDialogAnswerOption0"
        case .necronomiconStage5AnswerOption0: "NecronomiconStage5AnswerOption0"
        case .necronomiconStage5DogDialog: "NecronomiconStage5DogDialog"
        case .necronomiconStage6AnswerOption0: "NecronomiconStage6AnswerOption0"
        case .necronomiconStage6DogDialog: "NecronomiconStage6DogDialog"
        case .necronomiconStage7DogDialog0: "NecronomiconStage7DogDialog0"
        case .necronomiconStage7AnswerOption0: "NecronomiconStage7AnswerOption0"
        case .necronomiconStage7DogDialog1: "NecronomiconStage7DogDialog1"
        case .necronomiconStage8AnswerOption0: "NecronomiconStage8AnswerOption0"
        case .necronomiconStage8Dialog0: "NecronomiconStage8Dialog0"
        case .necronomiconStage8Dialog1: "NecronomiconStage8Dialog1"
        }
        
        return String(localized: resId)
    }
}
