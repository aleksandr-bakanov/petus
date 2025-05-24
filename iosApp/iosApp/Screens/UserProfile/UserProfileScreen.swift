import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct UserProfileView: View {
    
    @StateViewModel var viewModel: UserProfileScreenViewModel = UserProfileScreenViewModel()
    
    var body: some View {
        ScrollView {
            if let state = viewModel.uiState.value {
                VStack(alignment: .center, spacing: 16) {
                    if let weather = state.latestWeather {
                        Text(weather).font(.caption)
                    }
                    Text("ProfileScreenLanguagesLabel")
                    LanguageKnowledgeCell(type: .catus, value: state.languageKnowledgeCatus)
                    LanguageKnowledgeCell(type: .dogus, value: state.languageKnowledgeDogus)
                    LanguageKnowledgeCell(type: .frogus, value: state.languageKnowledgeFrogus)
                    Text("ProfileScreenInventoryLabel")
                    ForEach(state.inventory, id: \.id) { item in
                        InventoryItemCell(item: item)
                    }
                    Text("ProfileScreenAbilitiesLabel")
                    ForEach(state.abilities, id: \.self) { item in
                        Text(item.name)
                    }
                }
                .frame(maxWidth: .infinity) // Center horizontally
                .padding()
            }
        }
        .navigationTitle("ProfileScreenTitle")
    }
}
