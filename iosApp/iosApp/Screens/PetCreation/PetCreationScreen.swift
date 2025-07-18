import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct PetCreationScreen: View {
    @State private var name: String = ""
    @Environment(\.dismiss) private var dismiss
    
    @StateViewModel var viewModel: PetCreationScreenViewModel = PetCreationScreenViewModel()

    var body: some View {
        VStack {
            if let state = viewModel.uiState.value {
                ScrollView {
                    VStack(alignment: .center, spacing: 16) {
                        // Title: Select pet type
                        Text("PetCreationScreenSelectPetType")
                            .font(.largeTitle)
                            .frame(maxWidth: .infinity)
                            .padding(8)
                            .multilineTextAlignment(.center)
                        
                        // PetTypePicker
                        PetTypePicker(selectedValue: state.type,
                                      availablePetTypes: state.availablePetTypes) { newType in
                            viewModel.onAction(action: PetCreationScreenViewModelActionUpdateType(value: newType))
                        }
                        
                        // Title: Enter pet name
                        Text("PetCreationScreenEnterPetName")
                            .font(.largeTitle)
                            .frame(maxWidth: .infinity)
                            .padding(8)
                            .multilineTextAlignment(.center)
                        
                        // TextField for Pet Name
                        TextField("PetCreationScreenEnterPetNameHint", text: $name)
                            .textFieldStyle(.roundedBorder)
                            .font(.title2)
                            .padding(8)
                            .frame(maxWidth: .infinity)
                            .onChange(of: name) { newValue in
                                viewModel.onAction(action: PetCreationScreenViewModelActionUpdateName(value: newValue))
                            }
                        
                        // Type description
                        Text(state.typeDescription.localized)
                            .frame(maxWidth: .infinity)
                            .padding(16)
                            .multilineTextAlignment(.leading)
                        
                    }
                    .padding()
                }
                
                Spacer()
                
                // Create Button
                Button(action: {
                    viewModel.onAction(action: PetCreationScreenViewModelActionTapCreateButton())
                }) {
                    let buttonTitle = String(format: NSLocalizedString("PetCreationScreenButtonTemplate", comment: ""), petTypeString(state.type), state.name)
                    Text(buttonTitle)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                        .padding(8)
                }
            }
        }
        .task {
            for await navigation in viewModel.navigation {
                switch navigation {
                case is PetCreationScreenViewModelNavigationCloseScreen:
                    dismiss()
                case is PetCreationScreenViewModelNavigationPetCreationSuccess:
                    dismiss()
                default:
                    break
                }
            }
        }
        .navigationTitle("Create pet")
    }
}

private func petTypeString(_ type: PetType) -> String {
    switch type {
    case .catus:
        return NSLocalizedString("PetCreationScreenPetTypeCatus", comment: "")
    case .dogus:
        return NSLocalizedString("PetCreationScreenPetTypeDogus", comment: "")
    case .frogus:
        return NSLocalizedString("PetCreationScreenPetTypeFrogus", comment: "")
    case .bober:
        return NSLocalizedString("PetCreationScreenPetTypeBober", comment: "")
    }
}
