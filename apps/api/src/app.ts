import { swaggerUI } from "@hono/swagger-ui"
import { OpenAPIHono } from "@hono/zod-openapi"
import { healthRoute } from "./health"

export const app = new OpenAPIHono()

app.openapi(healthRoute, (context) => context.json({ status: "ok" as const }, 200))

export const openApiInfo = { openapi: "3.1.0", info: { title: "Harness API", version: "0.0.0" } } as const

app.doc31("/openapi.json", openApiInfo)
app.get("/docs", swaggerUI({ url: "/openapi.json" }))
