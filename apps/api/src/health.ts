import { createRoute, z } from "@hono/zod-openapi"

export const HealthResponse = z
  .object({ status: z.literal("ok"), database: z.literal("ok") })
  .openapi("HealthResponse")

export const HealthDegradedResponse = z
  .object({ status: z.literal("degraded"), database: z.literal("unreachable") })
  .openapi("HealthDegradedResponse")

export const healthRoute = createRoute({
  method: "get",
  path: "/health",
  responses: {
    200: {
      description: "The API and its database are up",
      content: { "application/json": { schema: HealthResponse } },
    },
    503: {
      description: "The API is up but its database is unreachable",
      content: { "application/json": { schema: HealthDegradedResponse } },
    },
  },
})
