//
//  OnboardingBottomSheet.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 26/01/2026.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct OnboardingBottomSheetView: View {

    let uiState: BottomSheetType.Onboarding

    var body: some View {
        VStack {
            let currentPage = Int(uiState.currentPage)
            // Main content
            VStack(spacing: 16) {
                Image(uiImage: UIImage(named: uiState.pages[currentPage].image.resId)!)
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: UIScreen.main.bounds.width * 0.7)
                    .clipShape(Circle())
                    .padding(.top, 32)

                Text(uiState.pages[currentPage].title.localized)
                    .font(.system(size: 36, weight: .regular))
                    .padding(.horizontal, 16)

                Text(uiState.pages[currentPage].message.localized)
                    .font(.system(size: 18))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)
            }

            Spacer()

            // Bottom bar
            HStack(alignment: .center, spacing: 10) {
                Text(uiState.leftButtonTitle.localized)
                    .font(.system(size: 16))
                    .onTapGesture {
                        uiState.leftButtonAction()
                    }

                ThreeDotsPagerIndicatorView(
                    totalDots: uiState.pages.count,
                    selectedIndex: currentPage
                )

                Text(uiState.rightButtonTitle.localized)
                    .font(.system(size: 16))
                    .onTapGesture {
                        uiState.rightButtonAction()
                    }
            }
            .padding(.bottom, 70)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
