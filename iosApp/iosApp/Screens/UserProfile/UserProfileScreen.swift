import SwiftUI

struct UserProfileView: View {
    
    @StateObject var viewModel = UserProfileViewModel()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .center, spacing: 16) {
                    Text("Languages")
                    LanguageKnowledgeCell(type: .catus, value: viewModel.uiState.languageKnowledgeCatus)
                    LanguageKnowledgeCell(type: .dogus, value: viewModel.uiState.languageKnowledgeDogus)
                    LanguageKnowledgeCell(type: .frogus, value: viewModel.uiState.languageKnowledgeFrogus)
                    Text("Inventory")
                    ForEach(viewModel.uiState.inventory, id: \.id) { item in
                        InventoryItemCell(item: item)
                    }
                    ActionButton(title: "Add", backgroundColor: Color("SatietyColor"), action: { viewModel.addItem() })
                    ActionButton(title: "Remove", backgroundColor: Color("SatietyColor"), action: { viewModel.removeItem() })
                }
                .frame(maxWidth: .infinity) // Center horizontally
                .padding()
            }
            .navigationTitle("Profile")
        }
        .task {
            await viewModel.loadData()
        }
    }
}
