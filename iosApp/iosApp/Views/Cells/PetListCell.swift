import SwiftUI
import shared

struct PetThumbnailUiData {
    var petImageName: String // Assume using image names instead of resource IDs
    var pet: Pet
    var satietyFraction: CGFloat
    var psychFraction: CGFloat
    var healthFraction: CGFloat
}

struct PetListCell: View {
    var data: PetThumbnailUiData
    var onClick: () -> Void

    var body: some View {
        let maxHeight: CGFloat = 96

        HStack {
            Image(data.petImageName)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: maxHeight, height: maxHeight)
                .clipped()

            VStack(alignment: .leading, spacing: 4) {
                Text(data.pet.name)
                    .font(.title3) // Similar to MaterialTheme.typography.titleLarge
                    .fontWeight(.bold)

                VStack(spacing: 0) {
                    StatBar(title: "SAT", color: Color("SatietyColor"), fraction: data.satietyFraction)
                    StatBar(title: "PSY", color: Color("PsychColor"), fraction: data.psychFraction)
                    StatBar(title: "HLT", color: Color("HealthColor"), fraction: data.healthFraction)
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
