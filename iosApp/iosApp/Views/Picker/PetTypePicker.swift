import SwiftUI
import shared

struct PetTypePicker: View {
    var selectedValue: PetType
    var availablePetTypes: [PetType]
    var onSelect: (PetType) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(availablePetTypes, id: \.ordinal) { item in
                    let isSelected = item == selectedValue
                    
                    ImageItem(
                        imageName: item.eggImageName,
                        isSelected: isSelected,
                        onClick: { onSelect(item) }
                    )
                }
            }
            .frame(height: 112)
            .frame(maxWidth: .infinity)
            .padding(.horizontal)
        }
    }
}

struct ImageItem: View {
    let imageName: String
    let isSelected: Bool
    let onClick: () -> Void

    var body: some View {
        let size: CGFloat = isSelected ? 96 : 72

        Image(imageName)
            .resizable()
            .scaledToFit()
            .frame(width: size, height: size)
            .background(Color(.systemBackground))
            .clipShape(Circle())
            .padding(8)
            .animation(.easeInOut(duration: 0.3), value: isSelected)
            .onTapGesture {
                onClick()
            }
    }
}
