import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct ItemDetailsScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    let itemId: InventoryItemId
    
    @StateViewModel var viewModel: ItemDetailsScreenViewModel
    
    init(itemId: InventoryItemId) {
        self.itemId = itemId
        _viewModel = StateViewModel(wrappedValue: ItemDetailsScreenViewModel(args: ItemDetailsScreenViewModelArgs(itemId: itemId)))
    }

    var body: some View {
        if let state = viewModel.uiState.value {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(spacing: 0) {
                        Image(state.imageId.resId)
                            .resizable()
                            .interpolation(.high)
                            .antialiased(true)
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                        
                        Text(state.descriptionId.localized)
                            .font(.body)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(16)
                    }
                }
                .padding()
            }
            .navigationTitle(state.title.localized)
            .task {
                for await navigation in viewModel.navigation {
                    switch navigation {
                    case is ItemDetailsScreenViewModelNavigationCloseScreen:
                        dismiss()
                    default:
                        break
                    }
                }
            }
        }
    }
}

