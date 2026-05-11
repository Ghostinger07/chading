export interface Task {
  id: string;
  title: string;
  /** HH:mm 24h, optional */
  time?: string;
  done: boolean;
}

export interface JournalEntry {
  /** ISO date, YYYY-MM-DD */
  date: string;
  title: string;
  body: string;
  /** BCP47 language tag, e.g. "en", "hi", "ja". Optional — content is free-form. */
  language?: string;
  tasks: Task[];
  updatedAt: number;
}

export interface JournalStore {
  version: 1;
  entries: Record<string, JournalEntry>;
}
