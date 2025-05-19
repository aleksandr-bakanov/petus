import SwiftUI
import shared

struct PetListCell: View {
    var data: PetThumbnailUiData
    var onClick: () -> Void

    var body: some View {
        let maxHeight: CGFloat = 96

        HStack {
            Image(data.petImageResId.resId)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: maxHeight, height: maxHeight)
                .clipped()

            VStack(alignment: .leading, spacing: 4) {
                Text(data.pet.name)
                    .font(.title3) // Similar to MaterialTheme.typography.titleLarge
                    .fontWeight(.bold)

                VStack(spacing: 0) {
                    StatBar(title: "SAT", color: Color("SatietyColor"), fraction: CGFloat(data.satietyFraction))
                    StatBar(title: "PSY", color: Color("PsychColor"), fraction: CGFloat(data.psychFraction))
                    StatBar(title: "HLT", color: Color("HealthColor"), fraction: CGFloat(data.healthFraction))
                }
            }
            .frame(maxHeight: .infinity, alignment: .top)
            .padding(.leading, 8)
        }
        .frame(height: maxHeight)
        .contentShape(Rectangle()) // Make entire row tappable
        .onTapGesture {
            onClick()
        }
    }
}
