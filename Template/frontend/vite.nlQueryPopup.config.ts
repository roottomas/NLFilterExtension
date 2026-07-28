import { defineConfig } from 'vite'
// @ts-ignore
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    build: {
        emptyOutDir: false,
        cssCodeSplit: true,
        assetsInlineLimit: 10000000,
        rollupOptions: {
            input: {
                "NLQueryPopup": "src/nlQueryPopup/main.tsx",
            },
            output: {
                format: "iife",
                entryFileNames: "[name].js",
                inlineDynamicImports: true,
            },
        },
    },
});
