import SwiftUI
import shared

struct PetCemeteryListCell: View {
    let data: PetThumbnailUiData
    let onClick: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Title
            Text(data.pet.name)
                .font(.title) // equivalent to titleLarge
                .lineLimit(1)
                .truncationMode(.tail)
                .frame(maxWidth: .infinity, alignment: .center)
            
            // Image
            Image(data.petImageResId.resId)
                .resizable()
                .scaledToFit()
                .aspectRatio(contentMode: .fill)
                .clipShape(
                    RoundedCorner(radius: 16, corners: [.topLeft, .topRight])
                )
        }
        .contentShape(Rectangle()) // expands the tap area to the entire VStack
        .onTapGesture {
            onClick()
        }
    }
}
