import { Box, Divider, MenuItem, Stack, TextField, Typography } from '@mui/material';
import { JournalEntry } from '../types';
import TaskList from './TaskList';
import { formatPretty } from '../utils';

interface Props {
  entry: JournalEntry;
  onChange: (next: JournalEntry) => void;
}

// A short list of suggested languages. The input is free-form — users may type any BCP47 tag
// or leave it blank. Content itself is fully Unicode and accepts any language/script.
const LANGUAGE_SUGGESTIONS: Array<{ code: string; label: string }> = [
  { code: '', label: 'Any / not specified' },
  { code: 'en', label: 'English' },
  { code: 'hi', label: 'हिन्दी (Hindi)' },
  { code: 'bn', label: 'বাংলা (Bengali)' },
  { code: 'ta', label: 'தமிழ் (Tamil)' },
  { code: 'ur', label: 'اردو (Urdu)' },
  { code: 'ar', label: 'العربية (Arabic)' },
  { code: 'es', label: 'Español' },
  { code: 'fr', label: 'Français' },
  { code: 'de', label: 'Deutsch' },
  { code: 'pt', label: 'Português' },
  { code: 'ru', label: 'Русский' },
  { code: 'zh', label: '中文' },
  { code: 'ja', label: '日本語' },
  { code: 'ko', label: '한국어' },
];

export default function EntryEditor({ entry, onChange }: Props) {
  const update = <K extends keyof JournalEntry>(key: K, value: JournalEntry[K]) =>
    onChange({ ...entry, [key]: value, updatedAt: Date.now() });

  return (
    <Stack spacing={2.5}>
      <Box>
        <Typography variant="overline" color="text.secondary">
          {formatPretty(entry.date)}
        </Typography>
        <TextField
          variant="standard"
          fullWidth
          placeholder="Give this day a title…"
          value={entry.title}
          onChange={(e) => update('title', e.target.value)}
          InputProps={{
            disableUnderline: true,
            sx: { fontSize: '1.6rem', fontWeight: 600, mt: 0.5 },
          }}
        />
      </Box>

      <TextField
        select
        size="small"
        label="Language"
        value={entry.language ?? ''}
        onChange={(e) => update('language', e.target.value)}
        sx={{ maxWidth: 260 }}
        helperText="You can also just type any language — content accepts all scripts."
      >
        {LANGUAGE_SUGGESTIONS.map((l) => (
          <MenuItem key={l.code || 'none'} value={l.code}>
            {l.label}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        multiline
        minRows={8}
        fullWidth
        placeholder="Write freely in any language… ✍️"
        value={entry.body}
        onChange={(e) => update('body', e.target.value)}
        // Let the browser auto-detect direction for RTL scripts like Arabic/Hebrew/Urdu.
        inputProps={{ dir: 'auto', lang: entry.language || undefined, spellCheck: true }}
        sx={{
          '& .MuiInputBase-root': {
            fontSize: '1.05rem',
            lineHeight: 1.65,
            alignItems: 'flex-start',
          },
        }}
      />

      <Divider />

      <TaskList tasks={entry.tasks} onChange={(tasks) => update('tasks', tasks)} />
    </Stack>
  );
}
