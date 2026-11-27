import { useQuery } from "@tanstack/react-query"
import { fetchHealth } from "./requests/health"

export const App = () => {
  const health = useQuery({ queryKey: ["health"], queryFn: fetchHealth })

  const message = health.isPending ? "API: checking" : health.isError ? "API unreachable" : `API: ${health.data.status}`

  return (
    <main>
      <h1>Harness</h1>
      <p role="status">{message}</p>
    </main>
  )
}
