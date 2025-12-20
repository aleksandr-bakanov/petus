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
        _viewModel = StateViewModel(wrappedValue: PetDetailsScreenViewModel(args: PetDetailsScreenViewModelArgs(petId: petId,
                    convertStringIdToString: convertStringIdToString
                )
            )
        )
    }

    var body: some View {
        if let state = viewModel.uiState.value {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(spacing: 0) {
                        Image(state.petImageResId.resId)
                            .resizable()
                            .interpolation(.high)
                            .antialiased(true)
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                        if state.showStatBars {
                            StatBar(color: Color("SatietyColor"),
                                    fraction: CGFloat(state.satietyFraction),
                                    icon: Image(systemName: "fork.knife"))
                            StatBar(color: Color("PsychColor"),
                                    fraction: CGFloat(state.psychFraction),
                                    icon: Image(systemName: "face.smiling.inverse"))
                            StatBar(color: Color("HealthColor"),
                                    fraction: CGFloat(state.healthFraction),
                                    icon: Image(systemName: "heart.fill"))
                        }
                        if state.showWillHatchSoon {
                            Text("WillHatchSoon")
                                .font(.title2) // equivalent to headlineMedium
                                .frame(maxWidth: .infinity, alignment: .center)
                                .padding(16)
                        }
                    }
                    
                    if state.isAnyButtonShown {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(alignment: .center, spacing: 16) {
                                if state.showSpeakButton {
                                    AnimatedImageButton(imageName: state.petType.speakButtonImageName,
                                                        title: NSLocalizedString("ButtonTitleSpeak", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapSpeakButton())
                                    }
                                }
                                
                                if state.showPoopButton {
                                    AnimatedImageButton(imageName: state.petType.poopButtonImageName,
                                                        title: NSLocalizedString("ButtonTitlePoop", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapPoopButton())
                                    }
                                }
                                
                                if state.showFeedButton {
                                    AnimatedImageButton(imageName: state.petType.feedButtonImageName,
                                                        title: NSLocalizedString("ButtonTitleFeed", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapFeedButton())
                                    }
                                }
                                
                                if state.showHealButton {
                                    AnimatedImageButton(imageName: state.petType.healButtonImageName,
                                                        title: NSLocalizedString("ButtonTitleHeal", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapHealButton())
                                    }
                                }
                                
                                if state.showPlayButton {
                                    AnimatedImageButton(imageName: state.petType.playButtonImageName,
                                                        title: NSLocalizedString("ButtonTitlePlay", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapPlayButton())
                                    }
                                }
                                
                                if state.showWakeUpButton {
                                    AnimatedImageButton(imageName: state.petType.wakeUpButtonImageName,
                                                        title: NSLocalizedString("ButtonTitleWakeUp", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapWakeUpButton())
                                    }
                                }
                                
                                if state.showBuryButton {
                                    AnimatedImageButton(imageName: state.petType.buryButtonImageName,
                                                        title: NSLocalizedString("ButtonTitleBury", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapBuryButton())
                                    }
                                }
                                
                                if state.showForgetButton {
                                    AnimatedImageButton(imageName: "forget_fractal",
                                                        title: NSLocalizedString("ButtonTitleForget", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapForgetButton())
                                    }
                                }
                                
                                if state.showResurrectButton {
                                    AnimatedImageButton(imageName: state.petType.resurrectButtonImageName,
                                                        title: NSLocalizedString("ButtonTitleResurrect", comment: "")) {
                                        viewModel.onAction(action: PetDetailsScreenViewModelActionTapResurrectButton())
                                    }
                                }
                            }
//                            .frame(height: 112)
                            .frame(maxWidth: .infinity)
                            .padding(.horizontal)
                        }
                    }
                    
                    if let lifespan = state.lifespan {
                        VStack(alignment: .leading, spacing: 0) {
                            // Lifespan text centered
                            Text(lifespan)
                                .font(.title2) // equivalent to headlineMedium
                                .frame(maxWidth: .infinity, alignment: .center)
                                .padding(16)

                            // History events
                            ForEach(state.historyEvents, id: \.self) { event in
                                Text(event)
                                    .font(.caption) // equivalent to bodySmall
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 16)
                            }
                        }
                    }

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

