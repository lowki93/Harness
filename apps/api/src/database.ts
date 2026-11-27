import { Kysely, PostgresDialect, sql } from "kysely"
import pg from "pg"

// The business tables are added here by the first feature that stores data.
export interface Database {}

export const createDatabase = (databaseUrl: string) => {
  const pool = new pg.Pool({
    connectionString: databaseUrl,
    connectionTimeoutMillis: 2000,
  })
  pool.on("error", (error) => {
    console.error(
      JSON.stringify({
        message: "database_pool_error",
        service_name: "harness-api",
        environment: process.env["NODE_ENV"] ?? "development",
        error: error.message,
        stack: error.stack,
      }),
    )
  })
  return new Kysely<Database>({ dialect: new PostgresDialect({ pool }) })
}

export const createCheckDatabase = (database: Kysely<Database>) => async () => {
  await sql`select 1`.execute(database)
}
