const { test } = require('node:test');
const assert = require('node:assert/strict');
const { parseLmiBatch, parseLmiResult } = require(process.env.W4_MODEL_PATH);
const sample = { occupationCode: 'SOC-15-1252', region: ' hcmc ', demandScore: 0, snapshotDate: '2026-11-01', openJobPostingsCount: 0 };
const batch = item => JSON.stringify({ items: [item] });
test('envelope, zero metrics, normalized region', () => {
  const { items } = parseLmiBatch(batch(sample));
  assert.equal(items[0].demandScore, 0);
  assert.equal(items[0].openJobPostingsCount, 0);
  assert.equal(items[0].region, 'HCMC');
});
test('array input normalizes to exact envelope', () => assert.equal(parseLmiBatch(JSON.stringify([sample])).items.length, 1));
test('positive ID and optional metrics', () => assert.equal(parseLmiBatch(batch({ occupationId: 1, demandScore: 100, growthRatePct: -999.99, competitiveIndex: 9.99, snapshotDate: '2024-02-29' })).items[0].occupationId, 1));
test('maximum batch accepted', () => assert.equal(parseLmiBatch(JSON.stringify({ items: Array(500).fill(sample) })).items.length, 500));
for (const [name, raw] of [
  ['malformed JSON','{'], ['empty batch','{"items":[]}'], ['extra envelope field','{"items":[],"other":1}'], ['oversized batch',JSON.stringify(Array(501).fill(sample))], ['null item','[null]'], ['missing occupation',batch({...sample,occupationCode:undefined})], ['unknown field',batch({...sample,sourceName:'x'})], ['missing score',batch({...sample,demandScore:undefined})], ['score over 100',batch({...sample,demandScore:101})], ['negative score',batch({...sample,demandScore:-1})], ['score precision',batch({...sample,demandScore:1.234})], ['string number',batch({...sample,demandScore:'50'})], ['fractional postings',batch({...sample,openJobPostingsCount:1.5})], ['negative postings',batch({...sample,openJobPostingsCount:-1})], ['oversized postings',batch({...sample,openJobPostingsCount:2147483648})], ['invalid calendar date',batch({...sample,snapshotDate:'2026-02-30'})], ['invalid date format',batch({...sample,snapshotDate:'11/26/2026'})], ['invalid growth',batch({...sample,growthRatePct:1000})], ['invalid competition',batch({...sample,competitiveIndex:10})], ['invalid ID',batch({...sample,occupationId:1.1})], ['oversized code',batch({...sample,occupationCode:'A'.repeat(51)})], ['blank identifier',batch({...sample,occupationCode:'  '})]
]) test(`reject ${name}`, () => assert.throws(() => parseLmiBatch(raw)));
const result = {totalRecordsProcessed:2,recordsInserted:1,recordsUpdated:1,timestamp:'2026-11-26T00:00:00Z'};
test('real response counts accepted', () => assert.deepEqual(parseLmiResult(result), result));
test('inconsistent counts rejected', () => assert.throws(() => parseLmiResult({...result,recordsUpdated:2})));
test('empty success payload rejected', () => assert.throws(() => parseLmiResult({})));
test('invalid timestamp rejected', () => assert.throws(() => parseLmiResult({...result,timestamp:'invalid'})));
