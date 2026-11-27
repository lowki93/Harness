import { describe, expect, test, vi } from "vitest"
import { DecodingError, HttpError, TransportError } from "./errors"
import { fetchHealth } from "./health"

describe("request errors", () => {
  test("returns the parsed body on a 2xx response", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ status: "ok", database: "ok" })))

    await expect(fetchHealth()).resolves.toEqual({ status: "ok", database: "ok" })
  })

  test("throws an HttpError carrying the status on a non-2xx response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => Response.json({ status: "degraded", database: "unreachable" }, { status: 503 })),
    )

    await expect(fetchHealth()).rejects.toMatchObject({ name: "HttpError", status: 503 })
    await expect(fetchHealth()).rejects.toBeInstanceOf(HttpError)
  })

  test("throws a TransportError when the fetch rejects", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Promise.reject(new TypeError("network down"))))

    await expect(fetchHealth()).rejects.toBeInstanceOf(TransportError)
  })

  test("throws a DecodingError when a 2xx body is not valid JSON", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => new Response("not json", { status: 200, headers: { "content-type": "application/json" } })),
    )

    await expect(fetchHealth()).rejects.toBeInstanceOf(DecodingError)
  })
})
