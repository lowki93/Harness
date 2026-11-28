import { describe, expect, test } from "bun:test"

import { parseConfig } from "../src/config"

describe("parseConfig", () => {
  test("parses a valid DATABASE_URL", () => {
    const config = parseConfig({ DATABASE_URL: "postgres://harness:harness@localhost:5432/harness" })

    expect(config.databaseUrl).toBe("postgres://harness:harness@localhost:5432/harness")
  })

  test("throws naming DATABASE_URL when it is missing", () => {
    expect(() => parseConfig({})).toThrow("DATABASE_URL")
  })

  test("throws naming DATABASE_URL when it is not a URL", () => {
    expect(() => parseConfig({ DATABASE_URL: "not a url" })).toThrow("DATABASE_URL")
  })
})
