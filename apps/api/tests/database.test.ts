import { EventEmitter } from "node:events"

import { afterEach, describe, expect, spyOn, test } from "bun:test"
import type pg from "pg"

import { createCheckDatabase, createPool } from "../src/database"

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

describe("createPool", () => {
  const spies: Array<{ mockRestore: () => void }> = []

  afterEach(() => {
    while (spies.length > 0) spies.pop()?.mockRestore()
  })

  test("logs an error emitted by a connected client instead of throwing", () => {
    const consoleError = spyOn(console, "error").mockImplementation(() => {})
    spies.push(consoleError)
    const pool = createPool("postgres://user:pass@localhost:5432/db")
    const client = new EventEmitter()

    pool.emit("connect", client)

    expect(() => client.emit("error", new Error("Connection terminated unexpectedly"))).not.toThrow()
    expect(consoleError).toHaveBeenCalledTimes(1)
    expect(String(consoleError.mock.calls[0]?.[0])).toContain("Connection terminated unexpectedly")
  })
})
