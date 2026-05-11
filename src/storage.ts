import { JournalEntry, JournalStore } from './types';

const KEY = 'chading.journal.v1';

function emptyStore(): JournalStore {
  return { version: 1, entries: {} };
}

export function loadStore(): JournalStore {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return emptyStore();
    const parsed = JSON.parse(raw) as JournalStore;
    if (!parsed || parsed.version !== 1 || typeof parsed.entries !== 'object') {
      return emptyStore();
    }
    return parsed;
  } catch {
    return emptyStore();
  }
}

export function saveStore(store: JournalStore): void {
  localStorage.setItem(KEY, JSON.stringify(store));
}

export function upsertEntry(store: JournalStore, entry: JournalEntry): JournalStore {
  return {
    ...store,
    entries: { ...store.entries, [entry.date]: entry },
  };
}

export function deleteEntry(store: JournalStore, date: string): JournalStore {
  const copy = { ...store.entries };
  delete copy[date];
  return { ...store, entries: copy };
}

export function sortedDates(store: JournalStore): string[] {
  return Object.keys(store.entries).sort((a, b) => (a < b ? 1 : -1));
}
