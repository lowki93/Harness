import { createApp, openApiInfo } from "../src/app"

// The contract does not depend on the database: a stub keeps generate free of DATABASE_URL.
const app = createApp({ checkDatabase: async () => {} })
const document = app.getOpenAPI31Document(openApiInfo)
const target = new URL("../../../packages/contract/openapi.json", import.meta.url)

await Bun.write(target, `${JSON.stringify(document, null, 2)}\n`)
