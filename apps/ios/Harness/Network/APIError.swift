import Foundation

/// Failures of the request layer. Decoding errors belong to the network services, not here.
enum APIError: Error, Equatable {
    case invalidURL
    case invalidResponse
    case httpStatus(Int)
    case transport(URLError)
}
