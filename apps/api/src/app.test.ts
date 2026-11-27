import { describe, expect, spyOn, test } from "bun:test"

import { z } from "@hono/zod-openapi"

import { createApp } from "./app"
import { HealthDegradedResponse, HealthResponse } from "./health"

const healthyApp = () => createApp({ checkDatabase: async () => {} })

describe("GET /health", () => {
  test("returns 200 ok when the database check resolves", async () => {
    const response = await healthyApp().request("/health")

    expect(response.status).toBe(200)
    const body = HealthResponse.parse(await response.json())
    expect(body).toEqual({ status: "ok", database: "ok" })
  })

  test("returns 503 degraded without error detail when the database check rejects", async () => {
    const logged = spyOn(console, "error").mockImplementation(() => {})
    const app = createApp({
      checkDatabase: async () => {
        throw new Error("connect ECONNREFUSED 127.0.0.1:5432 secret-host")
      },
    })

    const response = await app.request("/health")

    expect(response.status).toBe(503)
    const text = await response.text()
    expect(HealthDegradedResponse.parse(JSON.parse(text))).toEqual({
      status: "degraded",
      database: "unreachable",
    })
    expect(text).not.toContain("ECONNREFUSED")
    expect(logged).toHaveBeenCalledTimes(1)
    expect(JSON.stringify(logged.mock.calls[0])).toContain("ECONNREFUSED")
    logged.mockRestore()
  })
})

const OpenApiDocument = z.object({ paths: z.record(z.string(), z.unknown()) })

describe("GET /docs", () => {
  test("serves Swagger UI HTML pointing at the served document", async () => {
    const response = await healthyApp().request("/docs")

    expect(response.status).toBe(200)
    expect(response.headers.get("content-type")).toContain("text/html")
    expect(await response.text()).toContain("/openapi.json")
  })
})

describe("GET /openapi.json", () => {
  test("serves an OpenAPI document listing /health", async () => {
    const response = await healthyApp().request("/openapi.json")

    expect(response.status).toBe(200)
    const document = OpenApiDocument.parse(await response.json())
    expect(Object.keys(document.paths)).toContain("/health")
  })
})
