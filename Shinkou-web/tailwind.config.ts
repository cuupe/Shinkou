import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'PingFang SC', 'Microsoft YaHei', 'ui-sans-serif', 'system-ui']
      },
      colors: {
        brand: {
          50: '#eef3ff',
          100: '#dfe7ff',
          500: '#3d55e6',
          600: '#3046db',
          700: '#2738bd'
        },
        ink: '#111827'
      },
      boxShadow: {
        soft: '0 24px 70px rgba(31, 41, 55, 0.10)',
        card: '0 14px 35px rgba(55, 71, 137, 0.10)'
      }
    }
  },
  plugins: []
} satisfies Config;
