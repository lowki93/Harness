import Foundation
@testable import Harness

/// Records every request and replays a fixed `Result`, so the API client is tested without a network.
actor HTTPSessionMock: HTTPSession {
    private(set) var requests: [URLRequest] = []
    private let result: Result<(Data, URLResponse), any Error>

    init(result: Result<(Data, URLResponse), any Error>) {
        self.result = result
    }

    init(statusCode: Int, body: Data = Data(), url: URL = URL(string: "http://localhost:3000")!) {
        let response = HTTPURLResponse(url: url, statusCode: statusCode, httpVersion: nil, headerFields: nil)!
        self.init(result: .success((body, response)))
    }

    func data(for request: URLRequest) async throws -> (Data, URLResponse) {
        requests.append(request)
        return try result.get()
    }
}
