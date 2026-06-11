import { describe, it, expect } from 'vitest'
import { renderMarkdown } from '@/utils/markdown-renderer'

describe('Markdown Renderer', () => {
  it('UT-MD-01: should render bold text', () => {
    const result = renderMarkdown('**加粗文本**')
    expect(result).toContain('<strong>加粗文本</strong>')
  })

  it('UT-MD-02: should render list', () => {
    const result = renderMarkdown('- 项目1\n- 项目2')
    expect(result).toContain('<ul>')
    expect(result).toContain('<li>')
  })

  it('UT-MD-03: should render inline code', () => {
    const result = renderMarkdown('`code`')
    expect(result).toContain('<code>code</code>')
  })

  it('UT-MD-04: should render link', () => {
    const result = renderMarkdown('[链接](https://example.com)')
    expect(result).toContain('<a href="https://example.com"')
  })

  it('UT-MD-05: should render code block', () => {
    const result = renderMarkdown('```\nconsole.log(\'hi\')\n```')
    expect(result).toContain('<pre>')
    expect(result).toContain('<code>')
  })

  it('UT-MD-06: should pass through plain text', () => {
    const result = renderMarkdown('普通文本')
    expect(result).toContain('普通文本')
  })
})