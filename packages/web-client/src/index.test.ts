import { describe, expect, test } from "bun:test";
import { createClient } from "./index";

describe("createClient", () => {
  test("types GET /health from the contract and returns the status", async () => {
    const client = createClient({
      baseUrl: "http://localhost",
      fetch: async () => Response.json({ status: "ok" }),
    });

    const { data } = await client.GET("/health");

    expect(data?.status).toBe("ok");
  });

  test("rejects a path that is not in the contract at type level", () => {
    const client = createClient({ baseUrl: "http://localhost" });

    // @ts-expect-error "/unknown" is not a path of the contract
    const call = () => client.GET("/unknown");

    expect(typeof call).toBe("function");
  });
});
