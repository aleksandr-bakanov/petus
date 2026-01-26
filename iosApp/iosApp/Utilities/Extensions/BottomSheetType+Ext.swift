//
//  BottomSheetType+Ext.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 26/01/2026.
//  Copyright © 2026 orgName. All rights reserved.
//
import shared

extension BottomSheetType: Identifiable {
    public var id: String {
        switch onEnum(of: self) {
        case .onboarding:
            return "onboarding"
        }
    }
}
