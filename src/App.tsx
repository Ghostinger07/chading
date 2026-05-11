import { useEffect, useMemo, useState } from 'react';
import {
  AppBar,
  Box,
  Button,
  Container,
  Drawer,
  IconButton,
  Paper,
  Stack,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DownloadIcon from '@mui/icons-material/Download';
import DescriptionIcon from '@mui/icons-material/Description';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import Sidebar from './components/Sidebar';
import EntryEditor from './components/EntryEditor';
import { JournalStore } from './types';
import {
  deleteEntry,
  loadStore,
  saveStore,
  upsertEntry,
} from './storage';
import { downloadEntryMarkdown, downloadJSON, newEntry, todayISO } from './utils';
import { useThemeMode } from './theme';

const SIDEBAR_WIDTH = 320;

export default function App() {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const { mode, toggle } = useThemeMode();

  const [store, setStore] = useState<JournalStore>(() => loadStore());
  const [selectedDate, setSelectedDate] = useState<string>(() => {
    const s = loadStore();
    const dates = Object.keys(s.entries).sort((a, b) => (a < b ? 1 : -1));
    return dates[0] ?? todayISO();
  });
  const [drawerOpen, setDrawerOpen] = useState(false);

  // Persist to localStorage whenever the store changes.
  useEffect(() => {
    saveStore(store);
  }, [store]);

  const entry = store.entries[selectedDate];

  const ensureEntry = (date: string) => {
    setStore((s) => {
      if (s.entries[date]) return s;
      return upsertEntry(s, newEntry(date));
    });
    setSelectedDate(date);
    if (!isDesktop) setDrawerOpen(false);
  };

  const handleSelect = (date: string) => {
    setSelectedDate(date);
    if (!isDesktop) setDrawerOpen(false);
  };

  const handleDelete = (date: string) => {
    setStore((s) => {
      const next = deleteEntry(s, date);
      const remaining = Object.keys(next.entries).sort((a, b) => (a < b ? 1 : -1));
      setSelectedDate(remaining[0] ?? todayISO());
      return next;
    });
  };

  const sidebar = (
    <Sidebar
      store={store}
      selectedDate={selectedDate}
      onSelect={handleSelect}
      onNew={ensureEntry}
      onDelete={handleDelete}
    />
  );

  const stats = useMemo(() => {
    const entries = Object.values(store.entries);
    const totalTasks = entries.reduce((n, e) => n + e.tasks.length, 0);
    const doneTasks = entries.reduce(
      (n, e) => n + e.tasks.filter((t) => t.done).length,
      0,
    );
    return { entries: entries.length, totalTasks, doneTasks };
  }, [store]);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar
        position="fixed"
        elevation={0}
        color="inherit"
        sx={{
          zIndex: (t) => t.zIndex.drawer + 1,
          borderBottom: 1,
          borderColor: 'divider',
          backdropFilter: 'blur(8px)',
        }}
      >
        <Toolbar sx={{ gap: 1 }}>
          {!isDesktop && (
            <IconButton
              edge="start"
              onClick={() => setDrawerOpen(true)}
              aria-label="Open entries"
            >
              <MenuIcon />
            </IconButton>
          )}
          <AutoStoriesIcon color="primary" />
          <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: 0.2 }}>
            Chading
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
            daily journal · any language · with tasks
          </Typography>

          <Box sx={{ flex: 1 }} />

          <Tooltip title="Download this entry as Markdown">
            <span>
              <Button
                size="small"
                onClick={() => entry && downloadEntryMarkdown(entry)}
                startIcon={<DescriptionIcon />}
                disabled={!entry}
              >
                .md
              </Button>
            </span>
          </Tooltip>
          <Tooltip title="Download all journal data as JSON">
            <Button
              size="small"
              onClick={() => downloadJSON(store)}
              startIcon={<DownloadIcon />}
              variant="outlined"
            >
              Export
            </Button>
          </Tooltip>
          <Tooltip title={mode === 'dark' ? 'Switch to light' : 'Switch to dark'}>
            <IconButton onClick={toggle} aria-label="Toggle theme">
              {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      {isDesktop ? (
        <Drawer
          variant="permanent"
          open
          sx={{
            width: SIDEBAR_WIDTH,
            flexShrink: 0,
            '& .MuiDrawer-paper': {
              width: SIDEBAR_WIDTH,
              boxSizing: 'border-box',
              borderRight: 1,
              borderColor: 'divider',
            },
          }}
        >
          <Toolbar />
          {sidebar}
        </Drawer>
      ) : (
        <Drawer
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          sx={{
            '& .MuiDrawer-paper': { width: SIDEBAR_WIDTH, boxSizing: 'border-box' },
          }}
        >
          <Toolbar />
          {sidebar}
        </Drawer>
      )}

      <Box component="main" sx={{ flex: 1, minWidth: 0 }}>
        <Toolbar />
        <Container maxWidth="md" sx={{ py: { xs: 2, md: 4 } }}>
          {entry ? (
            <Paper
              elevation={0}
              sx={{
                p: { xs: 2, md: 4 },
                border: 1,
                borderColor: 'divider',
                borderRadius: 3,
              }}
            >
              <EntryEditor
                entry={entry}
                onChange={(next) => setStore((s) => upsertEntry(s, next))}
              />
            </Paper>
          ) : (
            <Paper
              elevation={0}
              sx={{
                p: { xs: 3, md: 6 },
                border: 1,
                borderColor: 'divider',
                borderRadius: 3,
                textAlign: 'center',
              }}
            >
              <AutoStoriesIcon sx={{ fontSize: 56, color: 'primary.main', mb: 1 }} />
              <Typography variant="h5" gutterBottom>
                Start your journal
              </Typography>
              <Typography color="text.secondary" sx={{ maxWidth: 520, mx: 'auto', mb: 3 }}>
                Create an entry for any day, write in any language, and add tasks with times.
                Everything stays on this device — and you can download it anytime.
              </Typography>
              <Stack direction="row" spacing={1} justifyContent="center">
                <Button variant="contained" onClick={() => ensureEntry(todayISO())}>
                  Write today&apos;s entry
                </Button>
              </Stack>
            </Paper>
          )}

          <Stack
            direction="row"
            spacing={2}
            sx={{ mt: 3, color: 'text.secondary' }}
            flexWrap="wrap"
            useFlexGap
          >
            <Typography variant="caption">Entries: {stats.entries}</Typography>
            <Typography variant="caption">
              Tasks: {stats.doneTasks}/{stats.totalTasks} done
            </Typography>
            <Typography variant="caption">Saved locally in your browser</Typography>
          </Stack>
        </Container>
      </Box>
    </Box>
  );
}
