import SwiftUI
import shared

struct MainScreenUiState {
    let isInitial: Bool
    let showCemetery: Bool?
}

@MainActor final class MainScreenViewModel: ObservableObject {
    let koinHelper: KoinHelper = KoinHelper()
    
    @Published var uiState = MainScreenUiState(isInitial: true,
                                               showCemetery: nil)
    
    func initialLoad() async {
        let petsFlow = koinHelper.getAllPetsInCemeteryFlow()
        
        for await pets in petsFlow {
            let newValue = pets.count > 0
            let newState = MainScreenUiState(isInitial: false,
                                             showCemetery: newValue)
            uiState = newState
        }
    }
}
