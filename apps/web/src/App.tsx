import { useQuery } from "@tanstack/react-query"
import { apiClient } from "./api"

const fetchHealth = async () => {
  const { data, error } = await apiClient.GET("/health")
  if (!data) throw new Error(`Health check failed: ${JSON.stringify(error)}`)
  return data
}

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
