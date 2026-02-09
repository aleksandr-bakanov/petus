import shared

extension PetType {
    var feedButtonImageName: String {
        switch self {
        case .catus: return "feed_cat"
        case .dogus: return "feed_dog"
        case .frogus: return "feed_frog"
        case .bober: return "feed_bober"
        case .fractal: return "feed_fractal"
        case .dragon: return "feed_dragon"
        case .alien: return "feed_alien"
        }
    }

    var playButtonImageName: String {
        switch self {
        case .catus: return "play_cat"
        case .dogus: return "play_dog"
        case .frogus: return "play_frog"
        case .bober: return "play_bober"
        case .fractal: return "play_fractal"
        case .dragon: return "play_dragon"
        case .alien: return "play_alien"
        }
    }

    var healButtonImageName: String {
        switch self {
        case .catus: return "heal_cat"
        case .dogus: return "heal_dog"
        case .frogus: return "heal_frog"
        case .bober: return "heal_bober"
        case .fractal: return "heal_fractal"
        case .dragon: return "heal_dragon"
        case .alien: return "heal_alien"
        }
    }

    var poopButtonImageName: String {
        switch self {
        case .catus: return "clean_up_cat"
        case .dogus: return "clean_up_dog"
        case .frogus: return "clean_up_frog"
        case .bober: return "clean_up_bober"
        case .fractal: return "clean_up_fractal"
        case .dragon: return "clean_up_dragon"
        case .alien: return "clean_up_dragon" // Aliens don't poop
        }
    }

    var wakeUpButtonImageName: String {
        switch self {
        case .catus: return "wake_up_cat"
        case .dogus: return "wake_up_dog"
        case .frogus: return "wake_up_frog"
        case .bober: return "wake_up_bober"
        case .fractal: return "wake_up_fractal"
        case .dragon: return "wake_up_dragon"
        case .alien: return "wake_up_alien"
        }
    }
    
    var buryButtonImageName: String {
        switch self {
        case .catus: return "bury_cat"
        case .dogus: return "bury_dog"
        case .frogus: return "bury_frog"
        case .bober: return "bury_bober"
        case .fractal: return "bury_bober" // Can't bury fractal
        case .dragon: return "bury_dragon"
        case .alien: return "bury_alien"
        }
    }
    
    var speakButtonImageName: String {
        switch self {
        case .catus: return "speak_cat"
        case .dogus: return "speak_dog"
        case .frogus: return "speak_frog"
        case .bober: return "speak_bober"
        case .fractal: return "speak_fractal"
        case .dragon: return "speak_dragon"
        case .alien: return "speak_alien"
        }
    }
    
    var resurrectButtonImageName: String {
        switch self {
        case .catus: return "resurrect_cat"
        case .dogus: return "resurrect_dog"
        case .frogus: return "resurrect_frog"
        case .bober: return "resurrect_bober"
        case .fractal: return "resurrect_bober" // Can't resurrect fractal
        case .dragon: return "resurrect_dragon"
        case .alien: return "resurrect_bober" // Can't resurrect alien
        }
    }
    
    var eggImageName: String {
        switch self {
        case .catus: return "catus_egg"
        case .dogus: return "dogus_egg"
        case .frogus: return "frogus_egg"
        case .bober: return "bober_egg"
        case .fractal: return "fractal_egg"
        case .dragon: return "dragon_egg"
        case .alien: return "alien_egg"
        }
    }
}
