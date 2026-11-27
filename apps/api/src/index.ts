import { z } from "@hono/zod-openapi"
import { app } from "./app"

const port = z.coerce.number().int().min(1).max(65535).default(3000).parse(process.env["PORT"])

export default { port, fetch: app.fetch }
