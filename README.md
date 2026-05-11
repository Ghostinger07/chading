<div align="center">

# Chading

**A daily journal that writes in your language.**

Material‑UI daily journal app. Write in any language or script, add timed tasks for the day,
and download your data whenever you want.

<p>
  <img src="https://img.shields.io/badge/React-18-61dafb?logo=react&logoColor=white" alt="React" />
  <img src="https://img.shields.io/badge/TypeScript-5-3178c6?logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Material--UI-5-007fff?logo=mui&logoColor=white" alt="MUI" />
  <img src="https://img.shields.io/badge/Vite-5-646cff?logo=vite&logoColor=white" alt="Vite" />
</p>

</div>

---

## What is it

Chading is a single-page journaling app. One entry per day. Each entry has:

- A **title** and a **free‑form body** — write in English, हिन्दी, العربية, 中文, 日本語, or anything else. Content is Unicode, and right‑to‑left scripts render correctly (direction is auto‑detected).
- An optional **language tag** (handy if you want to export and filter later).
- A list of **tasks**, each with an optional **time** and a done / not‑done state.

Everything is saved locally in your browser (no server, no accounts). You can **download** your journal as JSON (all entries) or export a single day as Markdown.

## Features

- **Material Design** UI with light / dark mode (remembers your preference).
- **Multilingual content** — any script, auto direction, per‑entry language tag.
- **Daily tasks with time** — pick any 24h time, mark done, auto‑sorted.
- **Sidebar** of all entries with per‑day task progress summary.
- **Responsive** — works on phone, tablet, desktop.
- **Export** — download all data as JSON, or a single day as Markdown.
- **Local‑first** — everything lives in `localStorage`. Your data stays yours.

## Getting started

```bash
# 1. Clone
git clone https://github.com/Ghostinger07/chading.git
cd chading

# 2. Install
npm install

# 3. Run the dev server
npm run dev

# 4. Build for production
npm run build
npm run preview
```

Requires Node.js 18+.

## Tech stack

| Layer      | Choice                                |
| ---------- | ------------------------------------- |
| UI         | React 18 + TypeScript                 |
| Styling    | Material UI (MUI) 5 + Emotion         |
| Icons      | @mui/icons-material                   |
| Dates      | dayjs                                 |
| Build tool | Vite 5                                |
| Storage    | Browser `localStorage`                |

## Project layout

```
src/
├── App.tsx                # Top-level layout (app bar, sidebar, main)
├── main.tsx               # React entry
├── theme.tsx              # MUI theme + light/dark mode provider
├── types.ts               # Entry / Task / Store types
├── storage.ts             # localStorage load/save helpers
├── utils.ts               # Date helpers, JSON + Markdown export
└── components/
    ├── Sidebar.tsx        # List of days + new-entry picker
    ├── EntryEditor.tsx    # Title, language, body, tasks
    └── TaskList.tsx       # Tasks with optional time
```

## Data format

Exporting produces a JSON file like:

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

Single-day Markdown export produces a standard checkbox list you can drop into any Markdown editor.

## Roadmap

- [x] Daily entries in any language
- [x] Tasks with time
- [x] Material Design + dark mode
- [x] JSON / Markdown export
- [ ] Import from JSON
- [ ] Cloud sync (optional)
- [ ] Search across entries

## License

MIT — do whatever you want, just keep the notice.
