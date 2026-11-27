import { describe, expect, test } from "bun:test"
import type pg from "pg"

import { createCheckDatabase } from "./database"

interface FakeClient {
  readonly query: (config: unknown) => Promise<unknown>
  readonly release: (destroy?: boolean) => void
}

const createFakePool = (query: FakeClient["query"]) => {
  const queries: unknown[] = []
  const releases: Array<boolean | undefined> = []
  const client: FakeClient = {
    query: (config) => {
      queries.push(config)
      return query(config)
    },
    release: (destroy) => {
      releases.push(destroy)
    },
  }
  const pool = { connect: async () => client } as unknown as pg.Pool
  return { pool, queries, releases }
}

describe("createCheckDatabase", () => {
  test("runs select 1 with a 2 s query timeout and returns the client to the pool", async () => {
    const { pool, queries, releases } = createFakePool(async () => ({}))

    await createCheckDatabase(pool)()

    expect(queries).toEqual([{ text: "select 1", query_timeout: 2000 }])
    expect(releases).toEqual([undefined])
  })

  test("destroys the client and rethrows when the query fails", async () => {
    const { pool, releases } = createFakePool(async () => {
      throw new Error("Query read timeout")
    })

    await expect(createCheckDatabase(pool)()).rejects.toThrow("Query read timeout")
    expect(releases).toEqual([true])
  })
})
