/**
 * Direct production verification runner for apiClient interceptors.
 * Bundles and executes scripts/verify-production-api.ts using Vite's built-in bundler
 * without adding external dependencies (like ts-node or vitest).
 */
const { build } = require('vite');
const fs = require('fs');
const path = require('path');

async function run() {
  const tempOut = path.resolve(__dirname, '../.temp-verify-out');
  try {
    await build({
      configFile: false,
      build: {
        ssr: true,
        write: true,
        outDir: tempOut,
        rollupOptions: {
          input: path.resolve(__dirname, './verify-production-api.ts'),
        },
      },
    });

    // Execute the bundled production test
    require(path.resolve(tempOut, 'verify-production-api.js'));
  } finally {
    // Clean up temporary bundle directory
    if (fs.existsSync(tempOut)) {
      fs.rmSync(tempOut, { recursive: true, force: true });
    }
  }
}

run().catch((err) => {
  console.error('Production verification runner error:', err);
  process.exit(1);
});
