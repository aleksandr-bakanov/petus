import SwiftUI
import shared

struct UserProfileUiState {
    let languageKnowledgeCatus: String
    let languageKnowledgeDogus: String
    let languageKnowledgeFrogus: String
    let inventory: [InventoryItem]
}

@MainActor final class UserProfileViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    
    @Published var uiState: UserProfileUiState = UserProfileUiState(languageKnowledgeCatus: "",
                                                                    languageKnowledgeDogus: "",
                                                                    languageKnowledgeFrogus: "",
                                                                    inventory: []
    )
    
    func addItem() {
        Task {
            try await koinHelper.addInventoryItem(item: InventoryItem(id: InventoryItemId.necronomicon, amount: Int32(1)))
        }
    }
    
    func removeItem() {
        Task {
            try await koinHelper.removeInventoryItem(item: InventoryItem(id: InventoryItemId.necronomicon, amount: Int32(1)))
        }
    }
    
    func loadData() async {
        let dataFlow = koinHelper.getUserProfileFlow()
        for await data in dataFlow {
            self.uiState = UserProfileUiState(languageKnowledgeCatus: "\(data.languageKnowledge.catus) / \(UserStats.companion.MAXIMUM_LANGUAGE_UI_KNOWLEDGE)",
                                              languageKnowledgeDogus: "\(data.languageKnowledge.dogus) / \(UserStats.companion.MAXIMUM_LANGUAGE_UI_KNOWLEDGE)",
                                              languageKnowledgeFrogus: "\(data.languageKnowledge.frogus) / \(UserStats.companion.MAXIMUM_LANGUAGE_UI_KNOWLEDGE)",
                                              inventory: data.inventory
            )
        }
    }
}
