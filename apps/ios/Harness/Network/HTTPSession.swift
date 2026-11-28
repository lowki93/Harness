import Foundation

/// Seam over `URLSession` so the API client can be tested without real requests.
protocol HTTPSession: Sendable {
    func data(for request: URLRequest) async throws -> (Data, URLResponse)
}

/// `URLSession.data(for:delegate:)` has a default delegate argument, so it cannot satisfy the protocol itself.
extension URLSession: HTTPSession {
    func data(for request: URLRequest) async throws -> (Data, URLResponse) {
        try await data(for: request, delegate: nil)
    }
}
