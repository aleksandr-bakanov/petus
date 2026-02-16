//
//  SectorMaskShape.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 16/02/2026.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI

struct SectorMaskShape: Shape {
    let percentage: CGFloat

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let center = CGPoint(x: rect.midX, y: rect.midY)
        let radius = min(rect.width, rect.height) / 2
        
        // Start angle: -90 degrees is the top (12 o'clock)
        let startAngle = Angle(degrees: -90 + Double(360 * percentage))
        let endAngle = Angle(degrees: 270) // Completes the circle to the top

        path.move(to: center)
        path.addArc(
            center: center,
            radius: radius,
            startAngle: startAngle,
            endAngle: endAngle,
            clockwise: false
        )
        path.closeSubpath()
        
        return path
    }
}
