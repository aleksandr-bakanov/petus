//
//  PetRowView.swift
//  iosApp
//
//  Created by Aleksandr Bakanov on 19/06/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

struct PetRowView: View {
    var pet: Pet
    
    var body: some View {
        Text("Pet \(pet.name)")
    }
}
