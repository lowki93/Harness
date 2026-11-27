import { createRoute, z } from "@hono/zod-openapi"

export const healthResponseSchema = z
  .object({ status: z.literal("ok") })
  .openapi("HealthResponse")

export const healthRoute = createRoute({
  method: "get",
  path: "/health",
  responses: {
    200: {
      description: "The API is up",
      content: { "application/json": { schema: healthResponseSchema } },
    },
  },
})
