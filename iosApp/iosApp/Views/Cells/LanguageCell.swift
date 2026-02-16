//
//  LanguageCell.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 16/02/2026.
//  Copyright © 2026 orgName. All rights reserved.
//
import SwiftUI
import shared

struct LanguageCell: View {
    let type: PetType
    let percentage: CGFloat

    var body: some View {
        ZStack {
            // Background Image
            Image(languageImageName)
                .resizable()
                .scaledToFill()
                .clipShape(Circle())
                .scaleEffect(percentage < 1.0 ? 0.95 : 1.0)
            
            // Masked Overlay Image
            if percentage < 1.0 {
                Image("question_mark")
                    .resizable()
                    .scaledToFill()
                    .clipShape(SectorMaskShape(percentage: percentage))
            }
        }
    }
    
    private var languageImageName: String {
        switch type {
        case .catus: return "speak_cat"
        case .dogus: return "speak_dog"
        case .frogus: return "speak_frog"
        case .bober: return "speak_bober"
        case .fractal: return "speak_fractal"
        case .dragon: return "speak_dragon"
        case .alien: return "speak_alien"
        }
    }
}
