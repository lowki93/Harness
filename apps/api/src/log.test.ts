import { describe, expect, spyOn, test } from "bun:test"

import { logError } from "./log"

describe("logError", () => {
  test("writes one JSON line with message, service, environment and error detail", () => {
    const logged = spyOn(console, "error").mockImplementation(() => {})

    logError("something_failed", new Error("boom"))

    expect(logged).toHaveBeenCalledTimes(1)
    const entry = JSON.parse(String(logged.mock.calls[0]?.[0]))
    expect(entry.message).toBe("something_failed")
    expect(entry.service_name).toBe("harness-api")
    expect(typeof entry.environment).toBe("string")
    expect(entry.error).toBe("boom")
    expect(entry.stack).toContain("boom")
    logged.mockRestore()
  })

  test("stringifies non-Error values without a stack", () => {
    const logged = spyOn(console, "error").mockImplementation(() => {})

    logError("something_failed", "plain")

    const entry = JSON.parse(String(logged.mock.calls[0]?.[0]))
    expect(entry.error).toBe("plain")
    expect(entry.stack).toBeUndefined()
    logged.mockRestore()
  })
})
