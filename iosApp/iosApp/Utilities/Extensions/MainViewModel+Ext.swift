//
//  MainViewModel+Ext.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 26/01/2026.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

extension MainViewModel {
    
    var bottomSheetBinding: Binding<BottomSheetType?> {
        Binding(
            get: { self.uiState.value?.bottomSheetType },
            set: { newValue in
                if newValue == nil {
                    self.onAction(action: MainViewModelActionHideBottomSheet())
                }
            }
        )
    }
}
