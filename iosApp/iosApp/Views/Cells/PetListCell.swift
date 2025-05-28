import SwiftUI
import shared

struct PetListCell: View {
    let data: PetThumbnailUiData
    let onClick: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            // Image
            Image(data.petImageResId.resId)
                .resizable()
                .scaledToFill()
                .frame(width: 96, height: 96)
                .clipShape(RoundedRectangle(cornerRadius: 8))

            // Info Column
            VStack(alignment: .leading, spacing: 4) {
                Text(data.pet.name)
                    .font(.title3)
                    .lineLimit(1)

                StatBar(color: Color("SatietyColor"), fraction: CGFloat(data.satietyFraction))
                StatBar(color: Color("PsychColor"), fraction: CGFloat(data.psychFraction))
                StatBar(color: Color("HealthColor"), fraction: CGFloat(data.healthFraction))
            }
            .frame(maxHeight: .infinity, alignment: .top)
        }
        .frame(maxWidth: .infinity, maxHeight: 96)
        .contentShape(Rectangle()) // expands tap area
        .onTapGesture {
            onClick()
        }
    }
}
