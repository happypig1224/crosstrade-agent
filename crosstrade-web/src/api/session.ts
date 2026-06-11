import { request } from '@/utils/http-client'
import type {
  SessionCreateRequest,
  SessionCreateResponse,
  SessionRecoverRequest,
  SessionRecoverResponse,
  SessionCloseRequest,
  Result,
} from '@/api/types'

export function createSession(data: SessionCreateRequest): Promise<Result<SessionCreateResponse>> {
  return request<SessionCreateResponse>('POST', '/session/create', data)
}

export function recoverSession(data: SessionRecoverRequest): Promise<Result<SessionRecoverResponse>> {
  return request<SessionRecoverResponse>('POST', '/session/recover', data)
}

export function closeSession(data: SessionCloseRequest): Promise<Result<void>> {
  return request<void>('POST', '/session/close', data)
}