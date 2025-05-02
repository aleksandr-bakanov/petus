import SwiftUI
import shared

struct PetDetailsScreen: View {
    
    let petId: Int64
    
    @StateObject var viewModel: PetDetailsViewModel
    
    init(petId: Int64) {
        self.petId = petId
        _viewModel = StateObject(wrappedValue: PetDetailsViewModel(petId: petId))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(spacing: 0) {
                    if !viewModel.uiState.petImage.isEmpty {
                        Image(viewModel.uiState.petImage)
                            .resizable()
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                        StatBar(title: "SAT", color: Color("SatietyColor"), fraction: viewModel.uiState.satietyFraction)
                        StatBar(title: "PSY", color: Color("PsychColor"), fraction: viewModel.uiState.psychFraction)
                        StatBar(title: "HLT", color: Color("HealthColor"), fraction: viewModel.uiState.healthFraction)
                    }
                }
                
                if viewModel.isAnyButtonShown() {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(alignment: .center, spacing: 16) {
                            if viewModel.showFeedButton {
                                AnimatedImageButton(imageName: viewModel.uiState.petType.feedButtonImageName) {
                                    viewModel.feedPet()
                                }
                            }

                            if viewModel.showHealButton {
                                AnimatedImageButton(imageName: viewModel.uiState.petType.healButtonImageName) {
                                    viewModel.healPetIllness()
                                }
                            }

                            if viewModel.showPlayButton {
                                AnimatedImageButton(imageName: viewModel.uiState.petType.playButtonImageName) {
                                    viewModel.playWithPet()
                                }
                            }

                            if viewModel.showPoopButton {
                                AnimatedImageButton(imageName: viewModel.uiState.petType.poopButtonImageName) {
                                    viewModel.cleanAfterPet()
                                }
                            }
                            
                            if viewModel.showWakeUpButton {
                                AnimatedImageButton(imageName: viewModel.uiState.petType.wakeUpButtonImageName) {
                                    viewModel.wakeUpPet()
                                }
                            }
                        }
                        .frame(height: 112)
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal)
                    }
                }
                
                VStack(alignment: .leading, spacing: 8) {
                    DetailText(viewModel.uiState.creationTime)
                    DetailText(viewModel.uiState.ageState)
                    DetailText(viewModel.uiState.sleepState)
                    DetailText(viewModel.uiState.satiety)
                    DetailText(viewModel.uiState.psych)
                    DetailText(viewModel.uiState.health)
                    DetailText(viewModel.uiState.illness)
                    DetailText(viewModel.uiState.pooped)
                    DetailText(viewModel.uiState.timeOfDeath)
                }
                .padding(.top, 16)
                
                Spacer()
            }
            .padding()
        }
        .navigationTitle(viewModel.uiState.title)
        .task {
            await viewModel.loadPet()
        }
    }
}

