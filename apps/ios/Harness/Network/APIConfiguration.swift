import Foundation

struct APIConfiguration: Sendable, Equatable {
    let baseURL: URL

    /// Base URL of the current build configuration.
    static var current: APIConfiguration {
        #if DEBUG
        APIConfiguration(baseURL: URL(string: "http://localhost:3000")!)
        #else
        // shortcut: no production API is deployed yet, so Release reuses the dev URL as a placeholder.
        APIConfiguration(baseURL: URL(string: "http://localhost:3000")!)
        #endif
    }
}
