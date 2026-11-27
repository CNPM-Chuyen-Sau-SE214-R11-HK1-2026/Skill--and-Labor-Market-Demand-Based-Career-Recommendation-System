const { spawnSync } = require('node:child_process');
const { mkdtempSync, rmSync } = require('node:fs');
const { tmpdir } = require('node:os');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const out = mkdtempSync(path.join(tmpdir(), 'w4-t5-'));
try {
  let cmd, args;
  try { cmd = process.execPath; args = [require.resolve('typescript/bin/tsc')]; }
  catch { cmd = process.platform === 'win32' ? 'tsc.cmd' : 'tsc'; args = []; }
  const compiled = spawnSync(cmd, [...args, '--strict', '--target', 'ES2020', '--module', 'commonjs', '--typeRoots', out, '--outDir', out, path.join(root, 'src/features/admin/model.ts')], { stdio:'inherit', shell: process.platform === 'win32' && cmd.endsWith('.cmd') });
  if (compiled.status !== 0) throw new Error(`TypeScript model compile failed: ${compiled.error ?? compiled.status}`);
  const tested = spawnSync(process.execPath, ['--test', path.join(root, 'tests/w4-t5-model.test.cjs')], { stdio:'inherit', env: {...process.env, W4_MODEL_PATH:path.join(out,'model.js')} });
  process.exitCode = tested.status ?? 1;
} catch (error) { console.error(error); process.exitCode = 1; }
finally { rmSync(out, {recursive:true,force:true}); }
