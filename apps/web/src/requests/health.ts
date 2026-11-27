import { apiClient } from "../api"
import { unwrap } from "./errors"

export const fetchHealth = () => unwrap(apiClient.GET("/health"))
