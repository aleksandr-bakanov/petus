import SwiftUI
import shared

extension StringId {
    var localized: String {
        let resId: String.LocalizationValue = switch onEnum(of: self) {
        case .petTypeDescriptionCatus: "PetTypeDescriptionCatus"
        case .petTypeDescriptionDogus: "PetTypeDescriptionDogus"
        case .petTypeDescriptionFrogus: "PetTypeDescriptionFrogus"
        case .whatIsGoingOnWithYou: "WhatIsGoingOnWithYou"
        case .beingBetter: "BeenBetter"
        case .byeBye: "ByeBye"
        case .iShouldGo: "IShouldGo"
        case .seeYa: "SeeYa"
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
        case .cemeteryScreenTitle: "CemeteryScreenTitle"
        case .profileScreenTitle: "ProfileScreenTitle"
        case .weatherScreenTitle: "WeatherScreenTitle"
        case .zooScreenTitle: "ZooScreenTitle"
        }
        
        return String(localized: resId)
    }
}
