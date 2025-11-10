import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct PetCreationScreen: View {
    @State private var name: String = ""
    @Environment(\.dismiss) private var dismiss
    
    @StateViewModel var viewModel: PetCreationScreenViewModel = PetCreationScreenViewModel()
    
    @State private var text: String = ""

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
                        HStack {
                            TextField("PetCreationScreenEnterPetNameHint", text: $text)
                                .font(.largeTitle)
                                .onChange(of: text) { newValue in
                                    viewModel.onAction(action: PetCreationScreenViewModelActionUpdateName(value: newValue))
                                }
                                .padding(8)
                                .frame(maxWidth: .infinity)

                            Button(action: {
                                viewModel.onAction(action: PetCreationScreenViewModelActionGetRandomName())
                            }) {
                                Image(systemName: "dice") // Replace with custom image if needed
                                    .resizable()
                                    .frame(width: 24, height: 24)
                                    .padding(.trailing, 18)
                                    .foregroundColor(.gray)
                            }
                            .frame(width: 54, height: 54)
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.4))
                        )
                        .padding(.horizontal, 8)
                        .onAppear {
                            text = state.name
                        }
                        .onChange(of: state.name) { newValue in
                            if newValue != text {
                                text = newValue
                            }
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
                    let buttonTitle = createButtonText(type: state.type, name: state.name)
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

private func createButtonText(type: PetType, name: String) -> String {
    switch type {
    case .fractal:
        return String(format: NSLocalizedString("PetCreationScreenButtonTemplateFractal", comment: ""), name)
    default:
        return String(format: NSLocalizedString("PetCreationScreenButtonTemplate", comment: ""), petTypeString(type), name)
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
    case .fractal:
        return NSLocalizedString("PetCreationScreenPetTypeFractal", comment: "")
    case .dragon:
        return NSLocalizedString("PetCreationScreenPetTypeDragon", comment: "")
    }
}
