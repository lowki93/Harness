import Foundation
import Testing
@testable import Harness

struct DefaultAPIClientTests {
    private struct TestEndpoint: Endpoint {
        var path = "/items"
        var method = HTTPMethod.get
        var queryItems = [URLQueryItem(name: "page", value: "2")]
    }

    private static func makeSUT(session: some HTTPSession) -> DefaultAPIClient {
        DefaultAPIClient(configuration: APIConfiguration(baseURL: URL(string: "http://localhost:3000")!), session: session)
    }

    enum Success {
        @Test static func givenSuccessResponse_whenSend_thenReturnsExactBody() async throws {
            let body = Data(#"{"status":"ok"}"#.utf8)
            let session = HTTPSessionMock(statusCode: 200, body: body)
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            let received = try await sut.send(TestEndpoint())

            #expect(received == body)
        }

        @Test static func givenEndpoint_whenSend_thenRequestHasURLAndMethod() async throws {
            let session = HTTPSessionMock(statusCode: 200)
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            _ = try await sut.send(TestEndpoint(method: .post))

            let request = try #require(await session.requests.first)
            #expect(request.url?.absoluteString == "http://localhost:3000/items?page=2")
            #expect(request.httpMethod == "POST")
        }

        @Test static func givenNoQueryItems_whenSend_thenURLHasNoQuery() async throws {
            let session = HTTPSessionMock(statusCode: 204)
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            _ = try await sut.send(TestEndpoint(queryItems: []))

            let request = try #require(await session.requests.first)
            #expect(request.url?.absoluteString == "http://localhost:3000/items")
        }
    }

    enum Failure {
        @Test(arguments: [400, 401, 404, 500, 503])
        static func givenFailureStatus_whenSend_thenThrowsHTTPStatus(statusCode: Int) async {
            let session = HTTPSessionMock(statusCode: statusCode)
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            await #expect(throws: APIError.httpStatus(statusCode)) {
                _ = try await sut.send(TestEndpoint())
            }
        }

        @Test static func givenNonHTTPResponse_whenSend_thenThrowsInvalidResponse() async {
            let response = URLResponse(url: URL(string: "http://localhost:3000")!, mimeType: nil, expectedContentLength: 0, textEncodingName: nil)
            let session = HTTPSessionMock(result: .success((Data(), response)))
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            await #expect(throws: APIError.invalidResponse) {
                _ = try await sut.send(TestEndpoint())
            }
        }

        @Test static func givenURLError_whenSend_thenThrowsTransport() async {
            let session = HTTPSessionMock(result: .failure(URLError(.timedOut)))
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            await #expect(throws: APIError.transport(URLError(.timedOut))) {
                _ = try await sut.send(TestEndpoint())
            }
        }

        @Test static func givenCancellation_whenSend_thenThrowsTransportCancelled() async {
            let session = HTTPSessionMock(result: .failure(CancellationError()))
            let sut = DefaultAPIClientTests.makeSUT(session: session)

            await #expect(throws: APIError.transport(URLError(.cancelled))) {
                _ = try await sut.send(TestEndpoint())
            }
        }
    }
}
