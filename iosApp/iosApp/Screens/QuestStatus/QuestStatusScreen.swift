import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct QuestStatusView: View {
    
    @StateViewModel var viewModel: QuestStatusViewModel = QuestStatusViewModel(
        convertStringIdToString: convertStringIdToString
    )
    
    var body: some View {
        ScrollView {
            let state = viewModel.uiState.value
            VStack(spacing: 8) {
                Spacer().frame(height: 8)

                ForEach(state.quests.indices, id: \.self) { index in
                    ExpandableQuestCell(quest: state.quests[index])
                }
            }
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
        }
        .navigationTitle("QuestsScreenTitle")
    }
}

struct ExpandableQuestCell: View {
    let quest: QuestDescription
    @State private var isExpanded = false

    var body: some View {
        VStack(alignment: .leading) {
            HStack(alignment: .center) {
                QuestTitleRow(title: quest.questName, message: quest.stagesDescription)

                Button(action: {
                    withAnimation {
                        isExpanded.toggle()
                    }
                }) {
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .padding(.leading, 8)
                }
            }
            .animation(.easeInOut, value: isExpanded)
            
            if isExpanded {
                Text(quest.questDescription)
                    .font(.body)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(8)
            }

            Spacer().frame(height: 8)
        }
    }
}

struct QuestTitleRow: View {
    let title: String
    let message: String

    var body: some View {
        HStack(alignment: .top) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .frame(maxWidth: .infinity, alignment: .leading)

            Text(message)
                .font(.subheadline)
                .frame(alignment: .trailing)
        }
        .padding(.horizontal, 8)
    }
}
