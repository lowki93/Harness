import Foundation

/// Composition root: the only place where concrete implementations are created and wired.
/// View models, repositories and services receive their dependencies through `init` and never read it.
/// Every `make…()` returns a new instance, without any DI framework.
@MainActor
final class AppDependencies {
    /// Shared network session, kept so connections are reused.
    private lazy var session = makeURLSession()

    func makeURLSession() -> URLSession {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 10
        return URLSession(configuration: configuration)
    }

    func makeAPIConfiguration() -> APIConfiguration {
        .current
    }

    func makeAPIClient() -> some APIClient {
        DefaultAPIClient(configuration: makeAPIConfiguration(), session: session)
    }

    func makeJSONDecoder() -> JSONDecoder {
        JSONDecoder()
    }
}
