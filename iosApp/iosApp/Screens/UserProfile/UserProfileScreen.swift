import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct UserProfileView: View {
    
    @StateViewModel var viewModel: UserProfileScreenViewModel = UserProfileScreenViewModel()
    
    var body: some View {
        ScrollView {
            if let state = viewModel.uiState.value {
                VStack(alignment: .center, spacing: 16) {
                    Text("Languages")
                    LanguageKnowledgeCell(type: .catus, value: state.languageKnowledgeCatus)
                    LanguageKnowledgeCell(type: .dogus, value: state.languageKnowledgeDogus)
                    LanguageKnowledgeCell(type: .frogus, value: state.languageKnowledgeFrogus)
                    Text("Inventory")
                    ForEach(state.inventory, id: \.id) { item in
                        InventoryItemCell(item: item)
                    }
                    Text("Abilities")
                    ForEach(state.abilities, id: \.self) { item in
                        Text(item.name)
                    }
                }
                .frame(maxWidth: .infinity) // Center horizontally
                .padding()
            }
        }
        .navigationTitle("Profile")
    }
}
