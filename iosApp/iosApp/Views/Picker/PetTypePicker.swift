import SwiftUI
import shared

struct PetTypePicker: View {
    @Binding var selectedValue: PetType
    var onSelect: (PetType) -> Void
    
    let ite = [PetType.catus, PetType.dogus, PetType.frogus]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(ite, id: \.ordinal) { item in
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
