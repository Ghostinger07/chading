import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Base path is configurable so the same build works from:
// - local dev / `vite preview`            (BASE_PATH unset → "/")
// - GitHub Pages under a project path     (BASE_PATH="/chading/")
// - any static host served from a subpath (BASE_PATH="/your/path/")
const base = process.env.BASE_PATH ?? '/';

export default defineConfig({
  base,
  plugins: [react()],
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
});
