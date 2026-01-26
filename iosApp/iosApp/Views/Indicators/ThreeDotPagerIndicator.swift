//
//  ThreeDotPagerIndicator.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 26/01/2026.
//  Copyright © 2026 orgName. All rights reserved.
//
import SwiftUI

struct ThreeDotsPagerIndicatorView: View {

    let totalDots: Int
    let selectedIndex: Int

    var selectedColor: Color = .blue
    var unSelectedColor: Color = .gray.opacity(0.4)
    var dotHeight: CGFloat = 12
    var dotWidth: CGFloat = 8
    var dotSpacing: CGFloat = 6

    var body: some View {
        HStack(spacing: dotSpacing) {
            ForEach(0..<totalDots, id: \.self) { index in
                RoundedRectangle(cornerRadius: 50)
                    .fill(index == selectedIndex ? selectedColor : unSelectedColor)
                    .frame(width: dotWidth, height: dotHeight)
            }
        }
    }
}
