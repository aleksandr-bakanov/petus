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
                    Image("user_profile_avatar")
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 112, height: 112)
                        .clipped()
                    Text("ProfileScreenLanguagesLabel")
                    LanguageKnowledgeCell(type: .catus, value: state.languageKnowledgeCatus)
                    LanguageKnowledgeCell(type: .dogus, value: state.languageKnowledgeDogus)
                    LanguageKnowledgeCell(type: .frogus, value: state.languageKnowledgeFrogus)
                    LanguageKnowledgeCell(type: .bober, value: state.languageKnowledgeBober)
                    Text("ProfileScreenInventoryLabel")
                    ForEach(state.inventory, id: \.id) { item in
                        InventoryItemCell(item: item)
                    }
                    Text("ProfileScreenAbilitiesLabel")
                    ForEach(state.abilities, id: \.self) { item in
                        Text(item.toStringId().localized)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    Text("ProfileScreenMiscLabel")
                    HStack {
                        Text("ZooSizeTitle")
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .layoutPriority(1)
                        Text(state.zooSize)
                            .frame(width: 80, alignment: .trailing) // Fixed width or proportional width
                    }
                }
                .frame(maxWidth: .infinity) // Center horizontally
                .padding()
            }
        }
        .navigationTitle("ProfileScreenTitle")
    }
}
