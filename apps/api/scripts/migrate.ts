import { promises as fs } from "node:fs"
import path from "node:path"
import { FileMigrationProvider, Migrator } from "kysely/migration"
import { parseConfig } from "../src/config"
import { createDatabase, createPool } from "../src/database"

const config = parseConfig(process.env)
const database = createDatabase(createPool(config.databaseUrl))

const migrator = new Migrator({
  db: database,
  provider: new FileMigrationProvider({
    fs,
    path,
    migrationFolder: path.resolve(import.meta.dir, "../migrations"),
  }),
})

const { error, results } = await migrator.migrateToLatest()

for (const result of results ?? []) {
  console.info(`migration ${result.migrationName}: ${result.status}`)
}
await database.destroy()

if (error) {
  console.error("migration failed:", error)
  process.exit(1)
}
console.info(`migrate done: ${results?.length ?? 0} applied`)
