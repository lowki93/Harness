import Foundation

/// Describes one API call. Declare one enum per resource that conforms to it.
protocol Endpoint: Sendable {
    var path: String { get }
    var method: HTTPMethod { get }
    var queryItems: [URLQueryItem] { get }
}

extension Endpoint {
    var method: HTTPMethod { .get }
    var queryItems: [URLQueryItem] { [] }
}
