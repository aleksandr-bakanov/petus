import SwiftUI
import shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
    @State private var petName: String = ""

	var body: some View {
        NavigationView {
            listView()
                .navigationTitle("Pets")
        }
        VStack {
            weatherView()
            
            TextField(
                "Pet name",
                text: $petName
            )
            
            Button(action: {
                print("Add Pet \(petName)")
                viewModel.addPet(name: petName)
            }) {
                Text("Add Pet")
                    .foregroundColor(.white)
                    .frame(width: 100, height: 50)
                    .background(Color.blue)
            }
        }
	}
    
    private func weatherView() -> AnyView {
        switch viewModel.weather {
        case .loading:
            return AnyView(Text("Loading weather...").multilineTextAlignment(.center))
        case .result(let weather):
            return AnyView(Text(weather.description()))
        case .error(let description):
            return AnyView(Text(description).multilineTextAlignment(.center))
        }
    }
    
    private func listView() -> AnyView {
        switch viewModel.pets {
        case .loading:
            return AnyView(Text("Loading...").multilineTextAlignment(.center))
        case .result(let pets):
            return AnyView(List(pets) { pet in
                PetRowView(pet: pet)
            })
        case .error(let description):
            return AnyView(Text(description).multilineTextAlignment(.center))
        }
    }
}

extension ContentView {
    enum LoadablePets {
        case loading
        case result([Pet])
        case error(String)
    }
    
    enum WeatherState {
        case loading
        case result(WeatherDto)
        case error(String)
    }
    
    @MainActor
    class ViewModel: ObservableObject {
        @Published var pets = LoadablePets.loading
        @Published var weather = WeatherState.loading
        
        let helper: KoinHelper = KoinHelper()
        
        init() {
            self.loadPets()
            //self.getWeather()
        }
        
        func getWeather() {
            Task {
                do {
                    self.weather = .loading
                    let dto = try await helper.getWeather(latitude: 52.3676, longitude: 4.9041)
                    self.weather = .result(dto)
                } catch {
                    self.weather = .error(error.localizedDescription)
                }
            }
        }
        
        func loadPets() {
            Task {
                do {
                    self.pets = .loading
                    let pets = try await helper.getPets()
                    self.pets = .result(pets)
                } catch {
                    self.pets = .error(error.localizedDescription)
                }
            }
        }
        
        func addPet(name: String) {
            Task {
                do {
                    try await helper.addPet(name: name)
                    self.pets = .loading
                    let pets = try await helper.getPets()
                    self.pets = .result(pets)
                } catch {
                    self.pets = .error(error.localizedDescription)
                }
            }
        }
    }
}

extension Pet: Identifiable { }
