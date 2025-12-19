import SwiftUI

struct CanPetsDieOfOldAgeRow: View {
    let value: Bool
    let onClick: (Bool) -> Void

    var body: some View {
        HStack(alignment: .center) {
            Text(NSLocalizedString("CanPetsDieOfOldAge", comment: ""))
                .frame(maxWidth: .infinity, alignment: .leading)

            Toggle(isOn: Binding(
                get: { value },
                set: { newValue in onClick(newValue) }
            )) {
                EmptyView()
            }
            .toggleStyle(SwitchToggleStyle(tint: .accentColor))
        }
        .padding(.horizontal)
    }
}
