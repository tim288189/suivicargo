/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#eff6ff',
          100: '#dbeafe',
          500: '#0f4c81',
          600: '#0c3e6a',
          700: '#093058'
        }
      }
    }
  },
  plugins: [],
  // Important : éviter les collisions avec PrimeNG
  corePlugins: {
    preflight: false
  }
};
