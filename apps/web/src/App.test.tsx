import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { render, screen, waitFor } from "@testing-library/react"
import { describe, expect, test, vi } from "vitest"
import { App } from "./App"

const renderApp = () => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  render(
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>,
  )
}

describe("App", () => {
  test("shows API: ok when the health call succeeds", async () => {
    const fetch = vi.fn(async (_request: Request) => Response.json({ status: "ok" }))
    vi.stubGlobal("fetch", fetch)

    renderApp()

    await waitFor(() => expect(screen.getByRole("status")).toHaveTextContent("API: ok"))
    expect(new URL(fetch.mock.calls[0]?.[0].url ?? "").pathname).toBe("/api/health")
  })

  test("shows API unreachable when the fetch rejects", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Promise.reject(new TypeError("network down"))))

    renderApp()

    await waitFor(() => expect(screen.getByRole("status")).toHaveTextContent("API unreachable"))
  })

  test("shows API unreachable when the API answers with a non-2xx status", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response("boom", { status: 500 })))

    renderApp()

    await waitFor(() => expect(screen.getByRole("status")).toHaveTextContent("API unreachable"))
  })
})
