import { defineConfig, presetUno, presetAttributify } from 'unocss'
import transformerDirectives from '@unocss/transformer-directives'

export default defineConfig({
  presets: [presetUno(), presetAttributify()],
  transformers: [transformerDirectives()],
  theme: {
    colors: {
      primary: '#1890ff',
      success: '#52c41a',
      warning: '#fa8c16',
      danger: '#ff4d4f',
      info: '#faad14',
    },
  },
})