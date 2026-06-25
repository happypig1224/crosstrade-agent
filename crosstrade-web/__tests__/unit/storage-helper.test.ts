import { describe, it, expect, beforeEach, vi } from 'vitest'
import * as storage from '@/utils/storage-helper'

describe('Storage Helper', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('UT-ST-01: should set and get value', () => {
    storage.set('key', { a: 1 })
    const result = storage.get<{ a: number }>('key')
    expect(result).toEqual({ a: 1 })
  })

  it('UT-ST-02: should remove value', () => {
    storage.set('key', 'value')
    storage.remove('key')
    expect(storage.get('key')).toBeNull()
  })

  it('UT-ST-03: should return null for nonexistent key', () => {
    expect(storage.get('nonexistent')).toBeNull()
  })

  it('UT-ST-04: should handle localStorage unavailability gracefully', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('not available')
    })

    storage.set('key', 'value')
    expect(storage.get('key')).toBeNull()

    spy.mockRestore()
  })
})