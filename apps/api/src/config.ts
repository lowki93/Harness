import { z } from "@hono/zod-openapi"

const DatabaseUrl = z.url({ protocol: /^postgres(ql)?$/ })

export const parseConfig = (env: Record<string, string | undefined>) => {
  const result = DatabaseUrl.safeParse(env["DATABASE_URL"])
  if (!result.success) {
    throw new Error(
      "DATABASE_URL is missing or invalid: expected a postgres:// connection URL (see the README)",
    )
  }
  return { databaseUrl: result.data }
}
