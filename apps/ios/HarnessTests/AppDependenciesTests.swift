import Foundation
import Testing
@testable import Harness

@MainActor
struct AppDependenciesTests {
    let sut = AppDependencies()

    @Test func whenMakeURLSession_thenRequestTimeoutIsTenSeconds() {
        #expect(sut.makeURLSession().configuration.timeoutIntervalForRequest == 10)
    }

    #if DEBUG
    @Test func givenDebugBuild_whenMakeAPIConfiguration_thenBaseURLIsLocalhost() {
        #expect(sut.makeAPIConfiguration().baseURL == URL(string: "http://localhost:3000"))
    }
    #endif
}
