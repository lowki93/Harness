import { z } from "@hono/zod-openapi"

const environment = z.string().default("development").parse(process.env["NODE_ENV"])

export const logError = (message: string, error: unknown) => {
  console.error(
    JSON.stringify({
      message,
      service_name: "harness-api",
      environment,
      error: error instanceof Error ? error.message : String(error),
      stack: error instanceof Error ? error.stack : undefined,
    }),
  )
}
