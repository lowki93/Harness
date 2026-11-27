import createOpenApiClient from "openapi-fetch"
import type { ClientOptions } from "openapi-fetch"

import type { paths } from "./schema"

export const createClient = (options: ClientOptions) => createOpenApiClient<paths>(options)

export type { components, paths } from "./schema"
