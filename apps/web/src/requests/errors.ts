export class HttpError extends Error {
  override readonly name = "HttpError"

  constructor(readonly status: number) {
    super(`Request failed with status ${status}`)
  }
}

export class TransportError extends Error {
  override readonly name = "TransportError"

  constructor(cause: unknown) {
    super("Request could not reach the API", { cause })
  }
}

export class DecodingError extends Error {
  override readonly name = "DecodingError"

  constructor(cause: unknown) {
    super("API response could not be decoded", { cause })
  }
}

interface ClientResult<TData> {
  readonly data?: TData | undefined
  readonly response: Response
}

// Maps an openapi-fetch call to its data or to one typed error.
export const unwrap = async <TData>(call: Promise<ClientResult<TData>>) => {
  const result = await call.catch((cause: unknown) => {
    throw cause instanceof SyntaxError ? new DecodingError(cause) : new TransportError(cause)
  })
  if (!result.response.ok) throw new HttpError(result.response.status)
  if (result.data === undefined) throw new DecodingError(new Error("Empty response body"))
  return result.data
}
