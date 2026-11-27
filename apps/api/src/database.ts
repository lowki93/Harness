import { Kysely, PostgresDialect, sql } from "kysely"
import pg from "pg"

import { logError } from "./log"

// The business tables are added here by the first feature that stores data.
export interface Database {}

export const createDatabase = (databaseUrl: string) => {
  const pool = new pg.Pool({
    connectionString: databaseUrl,
    connectionTimeoutMillis: 2000,
    query_timeout: 2000,
  })
  pool.on("error", (error) => {
    logError("database_pool_error", error)
  })
  return new Kysely<Database>({ dialect: new PostgresDialect({ pool }) })
}

export const createCheckDatabase = (database: Kysely<Database>) => async () => {
  await sql`select 1`.execute(database)
}
