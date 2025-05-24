import SwiftUI
import shared

struct LanguageKnowledgeCell: View {
    let type: PetType
    let value: String

    var body: some View {
        HStack {
            Text(title)
                .frame(maxWidth: .infinity, alignment: .leading)
                .layoutPriority(1)
            Text(value)
                .frame(width: 80, alignment: .trailing) // Fixed width or proportional width
        }
    }

    private var title: String {
        switch type {
        case .catus:
            return NSLocalizedString("LanguageKnowledgeTitleCatus", comment: "")
        case .dogus:
            return NSLocalizedString("LanguageKnowledgeTitleDogus", comment: "")
        case .frogus:
            return NSLocalizedString("LanguageKnowledgeTitleFrogus", comment: "")
        }
    }
}
