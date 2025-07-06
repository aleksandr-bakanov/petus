import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct QuestStatusView: View {
    
    @StateViewModel var viewModel: QuestStatusViewModel = QuestStatusViewModel(convertStringIdToString: { stringId in
        stringId.localized
    })
    
    var body: some View {
        ScrollView {
            let state = viewModel.uiState.value
            VStack(alignment: .center, spacing: 16) {
                Spacer().frame(height: 8)

                ForEach(state.quests, id: \.questName) { quest in
                    QuestTitleRow(title: quest.questName, message: quest.stagesDescription)

                    Text(quest.questDescription)
                        .font(.body)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(8)

                    Spacer().frame(height: 8)
                }
            }
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
        }
        .navigationTitle("QuestsScreenTitle")
    }
}

struct QuestTitleRow: View {
    let title: String
    let message: String

    var body: some View {
        HStack {
            Text(title)
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
                .layoutPriority(1)

            Text(message)
                .frame(width: 80, alignment: .trailing)
                .multilineTextAlignment(.trailing)
        }
    }
}
