import { useState } from 'react';
import {
  Box,
  Checkbox,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Stack,
  TextField,
  Button,
  Typography,
  Tooltip,
} from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddIcon from '@mui/icons-material/Add';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { Task } from '../types';
import { newId } from '../utils';

interface Props {
  tasks: Task[];
  onChange: (tasks: Task[]) => void;
}

export default function TaskList({ tasks, onChange }: Props) {
  const [title, setTitle] = useState('');
  const [time, setTime] = useState('');

  const add = () => {
    const t = title.trim();
    if (!t) return;
    onChange([...tasks, { id: newId(), title: t, time: time || undefined, done: false }]);
    setTitle('');
    setTime('');
  };

  const toggle = (id: string) =>
    onChange(tasks.map((t) => (t.id === id ? { ...t, done: !t.done } : t)));

  const remove = (id: string) => onChange(tasks.filter((t) => t.id !== id));

  const sorted = [...tasks].sort((a, b) => {
    if (a.done !== b.done) return a.done ? 1 : -1;
    if (a.time && b.time) return a.time.localeCompare(b.time);
    if (a.time) return -1;
    if (b.time) return 1;
    return 0;
  });

  return (
    <Box>
      <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 600 }}>
        Tasks
      </Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 1.5 }}>
        <TextField
          size="small"
          fullWidth
          placeholder="Add a task…"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              add();
            }
          }}
        />
        <TextField
          size="small"
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
          sx={{ width: { xs: '100%', sm: 140 } }}
          InputProps={{
            startAdornment: (
              <AccessTimeIcon fontSize="small" sx={{ mr: 0.5, opacity: 0.6 }} />
            ),
          }}
        />
        <Button
          variant="contained"
          onClick={add}
          startIcon={<AddIcon />}
          sx={{ minWidth: 100 }}
        >
          Add
        </Button>
      </Stack>

      {sorted.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 1 }}>
          No tasks yet. Add your first task above.
        </Typography>
      ) : (
        <List dense disablePadding>
          {sorted.map((t) => (
            <ListItem
              key={t.id}
              disableGutters
              secondaryAction={
                <Tooltip title="Delete task">
                  <IconButton edge="end" aria-label="delete" onClick={() => remove(t.id)}>
                    <DeleteOutlineIcon />
                  </IconButton>
                </Tooltip>
              }
              sx={{
                borderRadius: 2,
                px: 1,
                '&:hover': { bgcolor: 'action.hover' },
              }}
            >
              <ListItemIcon sx={{ minWidth: 36 }}>
                <Checkbox
                  edge="start"
                  checked={t.done}
                  onChange={() => toggle(t.id)}
                  inputProps={{ 'aria-label': `Toggle task ${t.title}` }}
                />
              </ListItemIcon>
              <ListItemText
                primary={
                  <Stack direction="row" spacing={1} alignItems="center">
                    {t.time && (
                      <Typography
                        component="span"
                        variant="caption"
                        sx={{
                          px: 0.75,
                          py: 0.25,
                          borderRadius: 1,
                          bgcolor: 'action.selected',
                          fontVariantNumeric: 'tabular-nums',
                        }}
                      >
                        {t.time}
                      </Typography>
                    )}
                    <Typography
                      component="span"
                      sx={{
                        textDecoration: t.done ? 'line-through' : 'none',
                        color: t.done ? 'text.disabled' : 'text.primary',
                      }}
                    >
                      {t.title}
                    </Typography>
                  </Stack>
                }
              />
            </ListItem>
          ))}
        </List>
      )}
    </Box>
  );
}
