import { swaggerUI } from "@hono/swagger-ui"
import { OpenAPIHono } from "@hono/zod-openapi"
import { healthRoute } from "./health"

export interface AppDependencies {
  readonly checkDatabase: () => Promise<void>
}

export const openApiInfo = { openapi: "3.1.0", info: { title: "Harness API", version: "0.0.0" } } as const

export const createApp = ({ checkDatabase }: AppDependencies) => {
  const app = new OpenAPIHono()

  app.openapi(healthRoute, async (context) => {
    try {
      await checkDatabase()
      return context.json({ status: "ok" as const, database: "ok" as const }, 200)
    } catch (error) {
      console.error(
        JSON.stringify({
          message: "health_database_check_failed",
          service_name: "harness-api",
          environment: process.env["NODE_ENV"] ?? "development",
          error: error instanceof Error ? error.message : String(error),
          stack: error instanceof Error ? error.stack : undefined,
        }),
      )
      return context.json({ status: "degraded" as const, database: "unreachable" as const }, 503)
    }
  })

  app.doc31("/openapi.json", openApiInfo)
  app.get("/docs", swaggerUI({ url: "/openapi.json" }))

  return app
}
