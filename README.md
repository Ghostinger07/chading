<div align="center">

<img src="./assets/banner.svg" alt="Lumen — a daily journal, in any language" width="100%" />

<br />

<p>
  <a href="https://ghostinger07.github.io/chading/">
    <img src="https://img.shields.io/badge/live-demo-FBBF24?style=for-the-badge&labelColor=0F0E13" alt="Live demo" />
  </a>
  <a href="https://github.com/Ghostinger07/chading/releases/latest">
    <img src="https://img.shields.io/github/v/release/Ghostinger07/chading?style=for-the-badge&label=download&color=D97706&labelColor=0F0E13" alt="Latest release" />
  </a>
  <a href="./LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-FEF3C7?style=for-the-badge&labelColor=0F0E13" alt="MIT License" />
  </a>
</p>

<p>
  <img src="https://img.shields.io/badge/React-18-61dafb?logo=react&logoColor=white&labelColor=0F0E13" alt="React" />
  <img src="https://img.shields.io/badge/TypeScript-5-3178c6?logo=typescript&logoColor=white&labelColor=0F0E13" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Material--UI-5-007fff?logo=mui&logoColor=white&labelColor=0F0E13" alt="MUI" />
  <img src="https://img.shields.io/badge/Vite-5-646cff?logo=vite&logoColor=white&labelColor=0F0E13" alt="Vite" />
</p>

</div>

---

> **Lumen** *(lat. "light")* — a quiet space to put down your day, in whatever language it lives in.

A single-page daily journal. One page per day. Write freely in **any language or script**, add **tasks with times**, and **own your data** — it never leaves your device.

---

## &#10024; Try it

| | |
|---|---|
| **Live demo** | [ghostinger07.github.io/chading](https://ghostinger07.github.io/chading/) |
| **Download** | [Latest release](https://github.com/Ghostinger07/chading/releases/latest) — grab `lumen-vX.Y.Z.zip`, unzip, open `index.html` |
| **Run the source** | `git clone`, `npm install`, `npm run dev` |

```bash
unzip lumen-v0.1.0.zip -d lumen
cd lumen
python3 -m http.server 8080     # then open http://localhost:8080
```

---

## &#10024; What&#39;s inside

<table>
<tr>
<td width="50%" valign="top">

### Write

- **Any language, any script.** Unicode first-class. Arabic, Hebrew, Urdu render right-to-left automatically via `dir="auto"`.
- **Optional language tag** per page (useful if you export and filter later).
- **Freeform title + body** &mdash; no opinions about what a journal "should" look like.

</td>
<td width="50%" valign="top">

### Plan

- **Tasks with times.** Each page carries its own task list, each task has an optional `HH:mm`.
- **Check things off.** Done tasks sink to the bottom, pending ones with times sort by time.
- Perfect for anything schedule-shaped &mdash; rehearsal cues, show run-sheets, study blocks, travel days.

</td>
</tr>
<tr>
<td width="50%" valign="top">

### Own

- **Local-first.** Everything lives in your browser&#39;s `localStorage`. No server, no account, no telemetry.
- **Export anytime.** One button for the whole journal as JSON, another for the current day as Markdown.
- **Portable.** The release `.zip` is pure static files &mdash; open it from disk or host it anywhere.

</td>
<td width="50%" valign="top">

### Feel

- **Dark "midnight & amber" palette** by default, warm parchment light mode.
- **Material You-style components** &mdash; accessible, responsive, keyboard-friendly.
- **Serif display + clean sans body** (Cormorant Garamond + Inter). Quiet by design.

</td>
</tr>
</table>

---

## &#10024; Download &amp; run

### From a release (no build required)

```bash
# 1. Grab the latest build
curl -LO https://github.com/Ghostinger07/chading/releases/latest/download/lumen-v0.1.0.zip

# 2. Unzip & serve
unzip lumen-v0.1.0.zip -d lumen && cd lumen
python3 -m http.server 8080

# 3. Open http://localhost:8080
```

### From source (for hacking)

```bash
git clone https://github.com/Ghostinger07/chading.git
cd chading
npm install
npm run dev      # dev server
npm run build    # production build → dist/
npm run preview  # preview the prod build
```

Requires **Node.js 18+**.

---

## &#10024; Stack

| Layer      | Choice                                |
| ---------- | ------------------------------------- |
| UI         | React 18 + TypeScript                 |
| Styling    | Material UI 5 + Emotion (custom theme)|
| Icons      | @mui/icons-material                   |
| Dates      | dayjs                                 |
| Build tool | Vite 5                                |
| Storage    | Browser `localStorage`                |
| CI         | GitHub Actions (Pages + Releases)     |

---

## &#10024; Project layout

```
.
├── assets/                     # banner + logo SVGs for the repo
├── public/favicon.svg
├── src/
│   ├── App.tsx                 # Top-level layout (app bar, sidebar, main)
│   ├── main.tsx                # React entry
│   ├── theme.tsx               # MUI theme + light/dark provider (Lumen palette)
│   ├── types.ts                # Entry / Task / Store types
│   ├── storage.ts              # localStorage load/save (+ legacy key migration)
│   ├── utils.ts                # Date helpers, JSON + Markdown export
│   └── components/
│       ├── BrandMark.tsx       # Lumen flame mark (inline SVG)
│       ├── Sidebar.tsx         # List of days + new-entry picker
│       ├── EntryEditor.tsx     # Title, language, body, tasks
│       └── TaskList.tsx        # Tasks with optional time
├── .github/workflows/
│   ├── deploy-pages.yml        # Auto-deploy to GitHub Pages on push to main
│   └── release.yml             # Build + zip + attach to a GitHub Release on v* tag
└── vite.config.ts              # Base path configurable via BASE_PATH env
```

---

## &#10024; Data format

Exporting the whole journal yields JSON like:

```jsonc
{
  "version": 1,
  "entries": {
    "2026-05-11": {
      "date": "2026-05-11",
      "title": "Rehearsal notes",
      "body": "आज का दिन बहुत अच्छा था…",
      "language": "hi",
      "tasks": [
        { "id": "…", "title": "Stage check", "time": "18:30", "done": true  },
        { "id": "…", "title": "Sound cue",   "time": "19:00", "done": false }
      ],
      "updatedAt": 1762900000000
    }
  }
}
```

Single-day Markdown export is a standard GitHub-flavored checkbox list &mdash; drop it into Obsidian, Notion, Bear, anywhere.

---

## &#10024; Releases

Two automated pipelines do the work:

- **`deploy-pages.yml`** &mdash; every push to `main` rebuilds and publishes to GitHub Pages at <https://ghostinger07.github.io/chading/>.
- **`release.yml`** &mdash; pushing a tag like `v0.1.0` builds a portable bundle, zips `dist/`, and creates a GitHub Release with `lumen-v0.1.0.zip` + `.tar.gz` attached.

To cut a release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

You can also run the Release workflow manually from the Actions tab to produce a build artifact without tagging.

---

## &#10024; Roadmap

- [x] Daily pages in any language
- [x] Tasks with time
- [x] "Midnight & amber" dark mode + parchment light mode
- [x] JSON &amp; Markdown export
- [x] GitHub Pages live demo + versioned Releases
- [ ] Import from JSON
- [ ] Full-text search across pages
- [ ] Optional end-to-end-encrypted cloud sync
- [ ] PWA install / offline
- [ ] Desktop build (Tauri)

---

<div align="center">

<sub>Built with &#9829; as a quiet place to write. MIT licensed.</sub>

</div>
