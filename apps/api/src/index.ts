import { z } from "@hono/zod-openapi"
import { createApp } from "./app"
import { parseConfig } from "./config"
import { createCheckDatabase, createDatabase } from "./database"

const port = z.coerce.number().int().min(1).max(65535).default(3000).parse(process.env["PORT"])
const config = parseConfig(process.env)

const app = createApp({ checkDatabase: createCheckDatabase(createDatabase(config.databaseUrl)) })

export default { port, fetch: app.fetch }
