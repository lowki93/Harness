import { app, openApiInfo } from "../src/app"

const document = app.getOpenAPI31Document(openApiInfo)
const target = new URL("../../../packages/contract/openapi.json", import.meta.url)

await Bun.write(target, `${JSON.stringify(document, null, 2)}\n`)
