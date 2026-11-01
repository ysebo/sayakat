import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const source = process.argv[2] ?? path.join(here, '..', '..', 'api', 'target', 'experiments')
const dataDir = path.join(here, 'data')
const chartDir = path.join(here, 'charts')
fs.mkdirSync(dataDir, { recursive: true })
fs.mkdirSync(chartDir, { recursive: true })

function readCsv(file) {
  const [header, ...rows] = fs.readFileSync(file, 'utf8').trim().split(/\r?\n/)
  const keys = header.split(',')
  return rows.map((r) => Object.fromEntries(r.split(',').map((v, i) => [keys[i], isNaN(Number(v)) ? v : Number(v)])))
}

for (const f of ['results.csv', 'convergence.csv']) fs.copyFileSync(path.join(source, f), path.join(dataDir, f))
const results = readCsv(path.join(dataDir, 'results.csv'))
const convergence = readCsv(path.join(dataDir, 'convergence.csv'))

const ALGORITHMS = ['GREEDY', 'TF_CH', 'TF_LS_10s', 'TF_LS_30s', 'HYBRID_10s', 'HYBRID_30s'].filter((a) =>
  results.some((r) => r.algorithm === a),
)
const LABELS = {
  GREEDY: 'Greedy',
  TF_CH: 'Timefold CH',
  TF_LS_10s: 'Timefold LS 10s',
  TF_LS_30s: 'Timefold LS 30s',
  HYBRID_10s: 'Hybrid 10s',
  HYBRID_30s: 'Hybrid 30s',
}
const COLORS = {
  GREEDY: '#a8a29e',
  TF_CH: '#d6d3d1',
  TF_LS_10s: '#5eead4',
  TF_LS_30s: '#0d9488',
  HYBRID_10s: '#fdba74',
  HYBRID_30s: '#ea580c',
}
const sizes = [...new Set(results.map((r) => r.pois))].sort((a, b) => a - b)
const seeds = [...new Set(results.map((r) => r.seed))]

const mean = (xs) => xs.reduce((a, b) => a + b, 0) / xs.length
const std = (xs) => {
  const m = mean(xs)
  return Math.sqrt(mean(xs.map((x) => (x - m) ** 2)))
}
const pick = (alg, size) => results.filter((r) => r.algorithm === alg && r.pois === size)
const value = (alg, size, seed) => results.find((r) => r.algorithm === alg && r.pois === size && r.seed === seed)?.value

function stats(alg, size) {
  const rows = pick(alg, size)
  return {
    value: mean(rows.map((r) => r.value)),
    valueStd: std(rows.map((r) => r.value)),
    travel: mean(rows.map((r) => r.travel_min)),
    visited: mean(rows.map((r) => r.visited)),
    time: mean(rows.map((r) => r.time_ms)),
    feasible: rows.filter((r) => r.feasible === 'true' || r.feasible === true).length,
    runs: rows.length,
    gains: seeds.map((s) => ((value(alg, size, s) - value('GREEDY', size, s)) / value('GREEDY', size, s)) * 100),
  }
}

const W = 760
const H = 380
const M = { top: 40, right: 20, bottom: 56, left: 64 }
const svg = (body, title) =>
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${W} ${H}" font-family="Inter, Helvetica, Arial, sans-serif" font-size="12">
<rect width="${W}" height="${H}" fill="#ffffff"/>
<text x="${M.left}" y="22" font-size="14" font-weight="600" fill="#0c0a09">${title}</text>
${body}
</svg>
`

function axisY(max, label, fmt = (v) => v) {
  const plotH = H - M.top - M.bottom
  let out = ''
  for (let i = 0; i <= 5; i++) {
    const v = (max / 5) * i
    const y = M.top + plotH - (v / max) * plotH
    out += `<line x1="${M.left}" x2="${W - M.right}" y1="${y}" y2="${y}" stroke="#e7e5e4"/>`
    out += `<text x="${M.left - 8}" y="${y + 4}" text-anchor="end" fill="#78716c">${fmt(Math.round(v))}</text>`
  }
  out += `<text transform="translate(16 ${M.top + plotH / 2}) rotate(-90)" text-anchor="middle" fill="#57534e">${label}</text>`
  return out
}

function legend(algs, y = H - 14) {
  let x = M.left
  return algs
    .map((a) => {
      const s = `<rect x="${x}" y="${y - 9}" width="10" height="10" rx="2" fill="${COLORS[a]}"/><text x="${x + 14}" y="${y}" fill="#44403c">${LABELS[a]}</text>`
      x += 30 + LABELS[a].length * 6.5
      return s
    })
    .join('')
}

function barChart() {
  const plotW = W - M.left - M.right
  const plotH = H - M.top - M.bottom
  const max = Math.ceil(Math.max(...sizes.flatMap((s) => ALGORITHMS.map((a) => stats(a, s).value))) / 200) * 200
  const group = plotW / sizes.length
  const bar = (group * 0.8) / ALGORITHMS.length
  let body = axisY(max, 'Mean collected value (medium score)')
  sizes.forEach((size, gi) => {
    ALGORITHMS.forEach((a, ai) => {
      const st = stats(a, size)
      const h = (st.value / max) * plotH
      const x = M.left + gi * group + group * 0.1 + ai * bar
      body += `<rect x="${x}" y="${M.top + plotH - h}" width="${bar - 2}" height="${h}" rx="2" fill="${COLORS[a]}"><title>${LABELS[a]}: ${st.value.toFixed(0)} ± ${st.valueStd.toFixed(0)}</title></rect>`
    })
    body += `<text x="${M.left + gi * group + group / 2}" y="${M.top + plotH + 18}" text-anchor="middle" fill="#44403c">${size} POIs</text>`
  })
  return svg(body + legend(ALGORITHMS), 'Route value by instance size (mean of 5 seeds)')
}

function gainChart() {
  const algs = ALGORITHMS.filter((a) => a !== 'GREEDY')
  const plotW = W - M.left - M.right
  const plotH = H - M.top - M.bottom
  const all = sizes.flatMap((s) => algs.map((a) => mean(stats(a, s).gains)))
  const lo = Math.floor(Math.min(0, ...all) / 10) * 10
  const hi = Math.ceil(Math.max(...all) / 10) * 10
  const y = (v) => M.top + plotH - ((v - lo) / (hi - lo)) * plotH
  let body = ''
  for (let v = lo; v <= hi; v += 10) {
    body += `<line x1="${M.left}" x2="${W - M.right}" y1="${y(v)}" y2="${y(v)}" stroke="${v === 0 ? '#57534e' : '#e7e5e4'}"/>`
    body += `<text x="${M.left - 8}" y="${y(v) + 4}" text-anchor="end" fill="#78716c">${v}%</text>`
  }
  body += `<text transform="translate(16 ${M.top + plotH / 2}) rotate(-90)" text-anchor="middle" fill="#57534e">Value gain over greedy</text>`
  const group = plotW / sizes.length
  const bar = (group * 0.8) / algs.length
  sizes.forEach((size, gi) => {
    algs.forEach((a, ai) => {
      const g = mean(stats(a, size).gains)
      const x = M.left + gi * group + group * 0.1 + ai * bar
      body += `<rect x="${x}" y="${Math.min(y(g), y(0))}" width="${bar - 2}" height="${Math.abs(y(g) - y(0))}" rx="2" fill="${COLORS[a]}"><title>${LABELS[a]}: ${g.toFixed(1)}%</title></rect>`
    })
    body += `<text x="${M.left + gi * group + group / 2}" y="${M.top + plotH + 18}" text-anchor="middle" fill="#44403c">${size} POIs</text>`
  })
  return svg(body + legend(algs), 'Mean value gain relative to the greedy baseline')
}

function convergenceChart(size) {
  const algs = ['TF_LS_30s', 'HYBRID_30s'].filter((a) => convergence.some((c) => c.algorithm === a))
  const plotW = W - M.left - M.right
  const plotH = H - M.top - M.bottom
  const tMax = 30000
  const series = []
  for (const a of algs) {
    for (const s of seeds) {
      const pts = convergence.filter((c) => c.algorithm === a && c.pois === size && c.seed === s && c.hard === 0)
      if (pts.length) series.push({ a, s, pts })
    }
  }
  const vMax = Math.ceil(Math.max(...series.flatMap((x) => x.pts.map((p) => p.medium))) / 100) * 100
  const x = (t) => M.left + (Math.min(t, tMax) / tMax) * plotW
  const y = (v) => M.top + plotH - (v / vMax) * plotH
  let body = axisY(vMax, 'Best feasible value')
  for (let t = 0; t <= tMax; t += 5000) {
    body += `<text x="${x(t)}" y="${M.top + plotH + 18}" text-anchor="middle" fill="#78716c">${t / 1000}s</text>`
  }
  for (const { a, pts } of series) {
    let d = ''
    pts.forEach((p, i) => {
      if (i > 0) d += ` L${x(p.time_ms)},${y(pts[i - 1].medium)}`
      d += `${i === 0 ? 'M' : ' L'}${x(p.time_ms)},${y(p.medium)}`
    })
    d += ` L${x(tMax)},${y(pts.at(-1).medium)}`
    body += `<path d="${d}" fill="none" stroke="${COLORS[a]}" stroke-width="1.6" opacity="0.85"/>`
  }
  return svg(body + legend(algs), `Convergence on ${size}-POI instances (each line is one seed)`)
}

fs.writeFileSync(path.join(chartDir, 'value-by-size.svg'), barChart())
fs.writeFileSync(path.join(chartDir, 'gain-vs-greedy.svg'), gainChart())
const largest = sizes.at(-1)
fs.writeFileSync(path.join(chartDir, `convergence-${largest}.svg`), convergenceChart(largest))
if (sizes.includes(100)) fs.writeFileSync(path.join(chartDir, 'convergence-100.svg'), convergenceChart(100))

const fmt = (v, d = 0) => v.toFixed(d)
let table = '| POIs | Algorithm | Value (mean ± sd) | Gain vs greedy | Visited | Driving, min | Feasible | Time |\n'
table += '|---:|---|---:|---:|---:|---:|---:|---:|\n'
for (const size of sizes) {
  for (const a of ALGORITHMS) {
    const st = stats(a, size)
    const gain = a === 'GREEDY' ? '—' : `${mean(st.gains) >= 0 ? '+' : ''}${fmt(mean(st.gains), 1)}%`
    const time = st.time < 1000 ? `${fmt(st.time)} ms` : `${fmt(st.time / 1000, 1)} s`
    table += `| ${size} | ${LABELS[a]} | ${fmt(st.value)} ± ${fmt(st.valueStd)} | ${gain} | ${fmt(st.visited, 1)} | ${fmt(st.travel)} | ${st.feasible}/${st.runs} | ${time} |\n`
  }
}

let wins = '| POIs | Timefold LS 30s beats greedy | Hybrid 30s beats greedy |\n|---:|---:|---:|\n'
for (const size of sizes) {
  const count = (a) => (results.some((r) => r.algorithm === a) ? `${stats(a, size).gains.filter((g) => g > 0).length}/${seeds.length}` : '—')
  wins += `| ${size} | ${count('TF_LS_30s')} | ${count('HYBRID_30s')} |\n`
}

let ttb = '| POIs | Algorithm | Mean time to best | Max time to best |\n|---:|---|---:|---:|\n'
for (const size of sizes) {
  for (const a of ['TF_LS_30s', 'HYBRID_30s']) {
    const lasts = seeds
      .map((s) => convergence.filter((c) => c.algorithm === a && c.pois === size && c.seed === s).at(-1)?.time_ms)
      .filter((v) => v != null)
    if (lasts.length) ttb += `| ${size} | ${LABELS[a]} | ${fmt(mean(lasts) / 1000, 1)} s | ${fmt(Math.max(...lasts) / 1000, 1)} s |\n`
  }
}

fs.writeFileSync(
  path.join(here, 'results.md'),
  `# Planner evaluation — generated results

Generated by \`node docs/evaluation/build-report.mjs\` from \`api/target/experiments\`.
Instances: synthetic, ${sizes.join(' / ')} POIs, ${seeds.length} seeds each, July trip, Balanced pace,
3–7 days, budget 15 000 KGS per day.

## Summary table

${table}
## How often the optimiser beats the baseline

${wins}
## Time to the best solution

${ttb}
## Charts

![Value by size](charts/value-by-size.svg)

![Gain over greedy](charts/gain-vs-greedy.svg)

![Convergence ${largest}](charts/convergence-${largest}.svg)
${sizes.includes(100) ? '\n![Convergence 100](charts/convergence-100.svg)\n' : ''}`,
)
console.log(`report written to ${path.relative(process.cwd(), here)}`)
