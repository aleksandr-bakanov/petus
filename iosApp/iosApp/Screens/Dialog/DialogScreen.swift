import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct DialogScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    let petId: Int64
    
    @StateViewModel var viewModel: DialogScreenViewModel
    
    init(id: Int64) {
        self.petId = id
        _viewModel = StateViewModel(wrappedValue: DialogScreenViewModel(args: DialogScreenViewModelArgs(petId: id,
                    convertStringIdToString: { stringId in
                        stringId.localized
                    }
                )
            )
        )
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let node = viewModel.uiState.value {
                DetailText(node.text)
                
                VStack(alignment: .leading, spacing: 2) {
                    ForEach(Array(node.answers.enumerated()), id: \.element) { index, answer in
                        ActionButton(title: answer,
                                     backgroundColor: Color("SatietyColor"),
                                     action: { viewModel.onAction(action: DialogScreenViewModelActionChooseDialogAnswer(index: Int32(index))) }
                        )
                    }
                }
            }
        }
        .task {
            for await navigation in viewModel.navigation {
                switch navigation {
                case is DialogScreenViewModelNavigationCloseScreen:
                    dismiss()
                default:
                    break
                }
            }
        }
    }
}
