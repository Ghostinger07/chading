import dayjs from 'dayjs';
import { JournalEntry, JournalStore } from './types';

export function todayISO(): string {
  return dayjs().format('YYYY-MM-DD');
}

export function formatPretty(date: string): string {
  const d = dayjs(date);
  return d.isValid() ? d.format('dddd, MMM D, YYYY') : date;
}

export function newId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

export function newEntry(date: string): JournalEntry {
  return {
    date,
    title: '',
    body: '',
    language: '',
    tasks: [],
    updatedAt: Date.now(),
  };
}

export function downloadJSON(store: JournalStore): void {
  const blob = new Blob([JSON.stringify(store, null, 2)], {
    type: 'application/json',
  });
  const url = URL.createObjectURL(blob);
  triggerDownload(url, `lumen-journal-${todayISO()}.json`);
  URL.revokeObjectURL(url);
}

export function downloadEntryMarkdown(entry: JournalEntry): void {
  const md = entryToMarkdown(entry);
  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  triggerDownload(url, `lumen-${entry.date}.md`);
  URL.revokeObjectURL(url);
}

export function entryToMarkdown(entry: JournalEntry): string {
  const lines: string[] = [];
  lines.push(`# ${entry.title || formatPretty(entry.date)}`);
  lines.push('');
  lines.push(`_Date: ${entry.date}${entry.language ? ` · Language: ${entry.language}` : ''}_`);
  lines.push('');
  if (entry.body.trim()) {
    lines.push(entry.body.trim());
    lines.push('');
  }
  if (entry.tasks.length) {
    lines.push('## Tasks');
    for (const t of entry.tasks) {
      const check = t.done ? 'x' : ' ';
      const time = t.time ? ` \`${t.time}\`` : '';
      lines.push(`- [${check}]${time} ${t.title}`);
    }
    lines.push('');
  }
  return lines.join('\n');
}

function triggerDownload(url: string, filename: string) {
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
}
