import { z } from "@hono/zod-openapi"
import { describe, expect, test } from "bun:test"
import { app } from "./app"
import { healthResponseSchema } from "./health"

describe("GET /health", () => {
  test("returns 200 with a body matching the response schema", async () => {
    const response = await app.request("/health")

    expect(response.status).toBe(200)
    const body = healthResponseSchema.parse(await response.json())
    expect(body).toEqual({ status: "ok" })
  })
})

const OpenApiDocument = z.object({ paths: z.record(z.string(), z.unknown()) })

describe("API docs", () => {
  test("serves Swagger UI HTML on /docs", async () => {
    const response = await app.request("/docs")

    expect(response.status).toBe(200)
    expect(response.headers.get("content-type")).toContain("text/html")
  })

  test("serves an OpenAPI document listing /health", async () => {
    const response = await app.request("/openapi.json")

    expect(response.status).toBe(200)
    const document = OpenApiDocument.parse(await response.json())
    expect(Object.keys(document.paths)).toContain("/health")
  })
})
