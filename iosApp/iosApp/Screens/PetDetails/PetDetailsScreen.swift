import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct PetDetailsScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    let petId: Int64
    let onNavigateToDialog: (Int64) -> Void
    
    @StateViewModel var viewModel: PetDetailsScreenViewModel
    
    init(petId: Int64, onNavigateToDialog: @escaping (Int64) -> Void) {
        self.petId = petId
        self.onNavigateToDialog = onNavigateToDialog
        _viewModel = StateViewModel(wrappedValue: PetDetailsScreenViewModel(args: PetDetailsScreenViewModelArgs(petId: petId)))
    }

    var body: some View {
        if let state = viewModel.uiState.value {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(spacing: 0) {
                        Image(state.petImageResId.resId)
                            .resizable()
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                        StatBar(title: "SAT", color: Color("SatietyColor"), fraction: CGFloat(state.satietyFraction))
                        StatBar(title: "PSY", color: Color("PsychColor"), fraction: CGFloat(state.psychFraction))
                        StatBar(title: "HLT", color: Color("HealthColor"), fraction: CGFloat(state.healthFraction))
                    }
                    
                    if state.isAnyButtonShown {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(alignment: .center, spacing: 16) {
                                if state.showFeedButton {
                                    AnimatedImageButton(imageName: state.petType.feedButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapFeedButton())
                                    }
                                }
                                
                                if state.showHealButton {
                                    AnimatedImageButton(imageName: state.petType.healButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapHealButton())
                                    }
                                }
                                
                                if state.showPlayButton {
                                    AnimatedImageButton(imageName: state.petType.playButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapPlayButton())
                                    }
                                }
                                
                                if state.showPoopButton {
                                    AnimatedImageButton(imageName: state.petType.poopButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapPoopButton())
                                    }
                                }
                                
                                if state.showWakeUpButton {
                                    AnimatedImageButton(imageName: state.petType.wakeUpButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapWakeUpButton())
                                    }
                                }
                                
                                if state.showBuryButton {
                                    AnimatedImageButton(imageName: state.petType.buryButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapBuryButton())
                                    }
                                }
                                
                                if state.showSpeakButton {
                                    AnimatedImageButton(imageName: state.petType.speakButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapSpeakButton())
                                    }
                                }
                                
                                if state.showResurrectButton {
                                    AnimatedImageButton(imageName: state.petType.resurrectButtonImageName) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapResurrectButton())
                                    }
                                }
                            }
                            .frame(height: 112)
                            .frame(maxWidth: .infinity)
                            .padding(.horizontal)
                        }
                    }
                    
//                    VStack(alignment: .leading, spacing: 8) {
//                        
//                        
//                        ActionButton(title: "Kill", backgroundColor: Color("SatietyColor"), action: { viewModel.kill() })
//                        ActionButton(title: "Resurrect", backgroundColor: Color("SatietyColor"), action: { viewModel.resurrectPet() })
//                        ActionButton(title: "Egg", backgroundColor: Color("SatietyColor"), action: { viewModel.changePetAgeState(state: .egg) })
//                        ActionButton(title: "Newborn", backgroundColor: Color("SatietyColor"), action: { viewModel.changePetAgeState(state: .newBorn) })
//                        ActionButton(title: "Adult", backgroundColor: Color("SatietyColor"), action: { viewModel.changePetAgeState(state: .adult) })
//                        ActionButton(title: "Old", backgroundColor: Color("SatietyColor"), action: { viewModel.changePetAgeState(state: .old) })
//                        ActionButton(title: "To cemetery", backgroundColor: Color("SatietyColor"), action: { viewModel.changePetPlace(place: .cemetery) })
//                        ActionButton(title: "To zoo", backgroundColor: Color("SatietyColor"), action: { viewModel.changePetPlace(place: .zoo) })
//                        
//                        DetailText(viewModel.uiState.creationTime)
//                        DetailText(viewModel.uiState.timeOfDeath)
//                    }
//                    .padding(.top, 16)
                    
                    Spacer()
                }
                .padding()
            }
            .navigationTitle(state.title)
            .task {
                for await navigation in viewModel.navigation {
                    switch navigation {
                    case is PetDetailsScreenViewModelNavigationCloseScreen:
                        dismiss()
                    case is PetDetailsScreenViewModelNavigationOpenDialogScreen:
                        if let nav = navigation as? PetDetailsScreenViewModelNavigationOpenDialogScreen {
                            onNavigateToDialog(nav.petId)
                        }
                    default:
                        break
                    }
                }
            }
        }
        
    }
}

