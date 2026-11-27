import { Kysely, PostgresDialect } from "kysely"
import pg from "pg"

import { logError } from "./log"

// The business tables are added here by the first feature that stores data.
export interface Database {}

// Only the connect timeout is set here: migrations and feature queries are not capped.
export const createPool = (databaseUrl: string) => {
  const pool = new pg.Pool({ connectionString: databaseUrl, connectionTimeoutMillis: 2000 })
  pool.on("error", (error) => {
    logError("database_pool_error", error)
  })
  // pg-pool drops its idle error listener while a client is checked out, so a connection drop
  // mid-query would otherwise emit an unhandled `error` and crash the process.
  pool.on("connect", (client) => {
    client.on("error", (error) => {
      logError("database_client_error", error)
    })
  })
  return pool
}

export const createDatabase = (pool: pg.Pool) =>
  new Kysely<Database>({ dialect: new PostgresDialect({ pool }) })

// @types/pg's QueryConfig omits the per-query `query_timeout` that pg supports.
interface TimedQueryConfig extends pg.QueryConfig {
  readonly query_timeout: number
}

const healthQuery: TimedQueryConfig = { text: "select 1", query_timeout: 2000 }

// A timed-out query is still in flight on its client, so a failed check destroys the client
// instead of returning it to the pool, where later checks would queue behind the dead query.
export const createCheckDatabase = (pool: pg.Pool) => async () => {
  const client = await pool.connect()
  try {
    await client.query(healthQuery)
    client.release()
  } catch (error) {
    client.release(true)
    throw error
  }
}
