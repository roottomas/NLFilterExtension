import { defineConfig } from 'vite'
// @ts-ignore
import react from '@vitejs/plugin-react'

// https://vite.dev/config/

export default defineConfig({
    plugins: [react()],
    build: {
        emptyOutDir: false,
        cssCodeSplit: true,
        assetsInlineLimit: 10000000,
        rollupOptions: {
            input: {
                "ExamplePopup": "src/examplePopup/main.tsx",
            },
            output: {
                format: "iife",
                entryFileNames: "[name].js",
                inlineDynamicImports: true,
            },
        },
    },
});