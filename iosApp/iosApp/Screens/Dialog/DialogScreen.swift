import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct DialogScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    @State private var scrollAnchor = UUID() // Used to scroll to bottom
    
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
        VStack(spacing: 0) {
            if let uiState = viewModel.uiState.value {
                // Scrollable message list
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 0) {
                            ForEach(uiState.messages.indices.reversed(), id: \.self) { index in
                                DialogMessageCell(message: uiState.messages[index])
                            }
                            
                            // Anchor at bottom for auto-scroll
                            Color.clear
                                .frame(height: 1)
                                .id(scrollAnchor)
                        }
                    }
                    .onChange(of: uiState.messages.count) { _ in
                        withAnimation {
                            proxy.scrollTo(scrollAnchor, anchor: .bottom)
                        }
                    }
                }
                
                Divider()
                
                // Bottom Buttons
                VStack(spacing: 8) {
                    ForEach(Array(uiState.answers.enumerated()), id: \.offset) { index, answer in
                        DialogButton(text: answer) {
                            viewModel.onAction(action: DialogScreenViewModelActionChooseDialogAnswer(index: Int32(index)))
                        }
                    }
                }
                .padding()
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
