import shared

extension PetType {
    var feedButtonImageName: String {
        switch self {
        case .catus: return "feed_cat"
        case .dogus: return "feed_dog"
        case .frogus: return "feed_frog"
        case .bober: return "feed_bober"
        }
    }

    var playButtonImageName: String {
        switch self {
        case .catus: return "play_cat"
        case .dogus: return "play_dog"
        case .frogus: return "play_frog"
        case .bober: return "play_bober"
        }
    }

    var healButtonImageName: String {
        switch self {
        case .catus: return "heal_cat"
        case .dogus: return "heal_dog"
        case .frogus: return "heal_frog"
        case .bober: return "heal_bober"
        }
    }

    var poopButtonImageName: String {
        switch self {
        case .catus: return "clean_up_cat"
        case .dogus: return "clean_up_dog"
        case .frogus: return "clean_up_frog"
        case .bober: return "clean_up_bober"
        }
    }

    var wakeUpButtonImageName: String {
        switch self {
        case .catus: return "wake_up_cat"
        case .dogus: return "wake_up_dog"
        case .frogus: return "wake_up_frog"
        case .bober: return "wake_up_bober"
        }
    }
    
    var buryButtonImageName: String {
        switch self {
        case .catus: return "bury_cat"
        case .dogus: return "bury_dog"
        case .frogus: return "bury_frog"
        case .bober: return "bury_bober"
        }
    }
    
    var speakButtonImageName: String {
        switch self {
        case .catus: return "speak_cat"
        case .dogus: return "speak_dog"
        case .frogus: return "speak_frog"
        case .bober: return "speak_bober"
        }
    }
    
    var resurrectButtonImageName: String {
        switch self {
        case .catus: return "resurrect_cat"
        case .dogus: return "resurrect_dog"
        case .frogus: return "resurrect_frog"
        case .bober: return "resurrect_bober"
        }
    }
    
    var eggImageName: String {
        switch self {
        case .catus: return "catus_egg"
        case .dogus: return "dogus_egg"
        case .frogus: return "frogus_egg"
        case .bober: return "bober_egg"
        }
    }
}
