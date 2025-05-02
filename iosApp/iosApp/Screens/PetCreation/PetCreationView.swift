import SwiftUI
import shared

struct PetCreationScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    @StateObject var viewModel = PetCreationViewModel()

    var body: some View {
        VStack {
            ScrollView {
                VStack(alignment: .center, spacing: 16) {
                    // Title: Select pet type
                    Text("Select pet type")
                        .font(.largeTitle)
                        .frame(maxWidth: .infinity)
                        .padding(8)
                        .multilineTextAlignment(.center)

                    // PetTypePicker
                    PetTypePicker(selectedValue: $viewModel.type) { newType in
                        viewModel.setNewType(type: newType)
                    }

                    // Title: Enter pet name
                    Text("Enter pet name:")
                        .font(.largeTitle)
                        .frame(maxWidth: .infinity)
                        .padding(8)
                        .multilineTextAlignment(.center)

                    // TextField for Pet Name
                    TextField(
                        "Pet name",
                        text: $viewModel.name
                    )
                    .textFieldStyle(.roundedBorder)
                    .font(.title2)
                    .padding(8)
                    .frame(maxWidth: .infinity)
                    
                    // Type description
                    Text(viewModel.typeDescription)
                        .frame(maxWidth: .infinity)
                        .padding(16)
                        .multilineTextAlignment(.leading)
                }
                .padding()
            }

            Spacer()

            // Create Button
            Button(action: {
                viewModel.createPet()
            }) {
                Text("Create \(viewModel.type.name) \(viewModel.name)")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .padding(8)
            }
        }
        .onAppear {
            viewModel.onFinish = {
                dismiss()
            }
        }
        .navigationTitle("Create pet")
    }
}

