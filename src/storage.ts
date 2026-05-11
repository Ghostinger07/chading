import { JournalEntry, JournalStore } from './types';

// New Lumen key, with a one-time migration from the previous "chading.*" key.
const KEY = 'lumen.journal.v1';
const LEGACY_KEY = 'chading.journal.v1';

function emptyStore(): JournalStore {
  return { version: 1, entries: {} };
}

export function loadStore(): JournalStore {
  try {
    const raw = localStorage.getItem(KEY) ?? localStorage.getItem(LEGACY_KEY);
    if (!raw) return emptyStore();
    const parsed = JSON.parse(raw) as JournalStore;
    if (!parsed || parsed.version !== 1 || typeof parsed.entries !== 'object') {
      return emptyStore();
    }
    // Migrate into new key on first read.
    if (!localStorage.getItem(KEY) && localStorage.getItem(LEGACY_KEY)) {
      localStorage.setItem(KEY, raw);
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
