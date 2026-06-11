import { describe, it, expect } from 'vitest'
import { sanitize } from '@/utils/xss-sanitizer'

describe('XSS Sanitizer', () => {
  it('UT-XSS-01: should remove script tags', () => {
    const result = sanitize('<script>alert(1)</script>')
    expect(result).not.toContain('<script')
    expect(result).not.toContain('alert(1)')
  })

  it('UT-XSS-02: should remove img onerror', () => {
    const result = sanitize('<img src=x onerror=alert(1)>')
    expect(result).not.toContain('onerror')
  })

  it('UT-XSS-03: should preserve valid markdown content', () => {
    const input = '<strong>加粗</strong> 和 <a href="https://example.com">链接</a>'
    const result = sanitize(input)
    expect(result).toContain('<strong>加粗</strong>')
    expect(result).toContain('href="https://example.com"')
  })

  it('UT-XSS-04: should remove iframe tags', () => {
    const result = sanitize('<iframe src="evil.com"></iframe>')
    expect(result).not.toContain('<iframe')
  })

  it('UT-XSS-05: should remove event handlers', () => {
    const result = sanitize('<div onclick="alert(1)">text</div>')
    expect(result).not.toContain('onclick')
  })
})