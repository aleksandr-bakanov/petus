package bav.petus.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import bav.petus.model.Pet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: PetsViewModel,
    requestBackgroundLocationPermission: () -> Unit,
) {
    val state by remember { viewModel.state }

    MyApplicationTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        "Pets",
                        style = MaterialTheme.typography.headlineLarge
                    )
                })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.showRationale) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = "GIVE ACCESS TO BACKGROUND LOCATION",
                    )
                    Button(onClick = {
                        requestBackgroundLocationPermission()
                        viewModel.updateRationale(value = false)
                    }) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                            ,
                            text = "Give permission"
                        )
                    }
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    value = state.newPetName,
                    onValueChange = {
                        viewModel.setNewPetName(it)
                    }
                )
                Button(onClick = {
                    viewModel.addPet()
                    viewModel.loadPets()
                }) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                        ,
                        text = "Add pet named ${state.newPetName}"
                    )
                }
                LazyColumn {
                    items(state.pets) { pet: Pet ->
                        Text(text = "Pet ${pet.name}")
                    }
                }
            }
        }
    }
}