import { createClient } from "@harness/web-client"

export const apiClient = createClient({
  baseUrl: new URL("/api", window.location.origin).toString(),
  fetch: (request) => globalThis.fetch(request),
})
