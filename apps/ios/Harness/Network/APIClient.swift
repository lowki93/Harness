import Foundation

/// Sends requests and returns the raw response body. Decoding is the job of the network services.
protocol APIClient: Sendable {
    func send(_ endpoint: some Endpoint) async throws(APIError) -> Data
}

struct DefaultAPIClient: APIClient {
    private let configuration: APIConfiguration
    private let session: any HTTPSession

    init(configuration: APIConfiguration, session: any HTTPSession) {
        self.configuration = configuration
        self.session = session
    }

    func send(_ endpoint: some Endpoint) async throws(APIError) -> Data {
        let request = try makeRequest(for: endpoint)
        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(for: request)
        } catch let error as URLError {
            throw .transport(error)
        } catch is CancellationError {
            throw .transport(URLError(.cancelled))
        } catch {
            throw .transport(URLError(.unknown))
        }

        guard let httpResponse = response as? HTTPURLResponse else {
            throw .invalidResponse
        }
        guard (200..<300).contains(httpResponse.statusCode) else {
            throw .httpStatus(httpResponse.statusCode)
        }
        return data
    }

    private func makeRequest(for endpoint: some Endpoint) throws(APIError) -> URLRequest {
        let url = configuration.baseURL.appending(path: endpoint.path)
        guard var components = URLComponents(url: url, resolvingAgainstBaseURL: false) else {
            throw .invalidURL
        }
        if !endpoint.queryItems.isEmpty {
            components.queryItems = endpoint.queryItems
        }
        guard let finalURL = components.url else {
            throw .invalidURL
        }
        var request = URLRequest(url: finalURL)
        request.httpMethod = endpoint.method.rawValue
        return request
    }
}
