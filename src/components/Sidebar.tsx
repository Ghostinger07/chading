import {
  Box,
  Button,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import { JournalStore } from '../types';
import { formatPretty } from '../utils';

interface Props {
  store: JournalStore;
  selectedDate: string;
  onSelect: (date: string) => void;
  onNew: (date: string) => void;
  onDelete: (date: string) => void;
}

export default function Sidebar({ store, selectedDate, onSelect, onNew, onDelete }: Props) {
  const dates = Object.keys(store.entries).sort((a, b) => (a < b ? 1 : -1));

  return (
    <Stack spacing={1.5} sx={{ p: 2, height: '100%' }}>
      <Typography variant="overline" color="text.secondary">
        New entry
      </Typography>
      <Stack direction="row" spacing={1}>
        <TextField
          size="small"
          type="date"
          value={selectedDate}
          onChange={(e) => onSelect(e.target.value)}
          fullWidth
        />
        <Tooltip title="Create or open this date">
          <Button
            variant="contained"
            onClick={() => onNew(selectedDate)}
            sx={{ minWidth: 40, px: 1 }}
          >
            <AddIcon />
          </Button>
        </Tooltip>
      </Stack>

      <Divider sx={{ my: 1 }} />

      <Typography variant="overline" color="text.secondary">
        Your journal
      </Typography>
      <Box sx={{ flex: 1, overflowY: 'auto', mx: -1 }}>
        {dates.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ px: 1, py: 2 }}>
            No entries yet. Pick a date above and start writing.
          </Typography>
        ) : (
          <List dense disablePadding>
            {dates.map((d) => {
              const e = store.entries[d];
              const subtitle =
                e.title?.trim() || (e.body?.trim().slice(0, 60) ?? '') || 'Untitled';
              const taskSummary =
                e.tasks.length > 0
                  ? `${e.tasks.filter((t) => t.done).length}/${e.tasks.length} tasks`
                  : '';
              return (
                <ListItem
                  key={d}
                  disablePadding
                  sx={{ px: 1, mb: 0.5 }}
                  secondaryAction={
                    <Tooltip title="Delete entry">
                      <IconButton
                        size="small"
                        edge="end"
                        onClick={(ev) => {
                          ev.stopPropagation();
                          if (confirm(`Delete entry for ${formatPretty(d)}?`)) onDelete(d);
                        }}
                      >
                        <DeleteOutlineIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  }
                >
                  <ListItemButton
                    selected={d === selectedDate}
                    onClick={() => onSelect(d)}
                    sx={{ borderRadius: 2, pr: 5 }}
                  >
                    <ListItemText
                      primary={formatPretty(d)}
                      secondary={
                        <Box component="span" sx={{ display: 'block' }}>
                          <Box component="span" sx={{ display: 'block' }}>
                            {subtitle}
                          </Box>
                          {taskSummary && (
                            <Box
                              component="span"
                              sx={{ display: 'block', fontSize: 11, opacity: 0.7 }}
                            >
                              {taskSummary}
                            </Box>
                          )}
                        </Box>
                      }
                      primaryTypographyProps={{ fontWeight: 500 }}
                      secondaryTypographyProps={{
                        component: 'span',
                        noWrap: true,
                        sx: { display: 'block' },
                      }}
                    />
                  </ListItemButton>
                </ListItem>
              );
            })}
          </List>
        )}
      </Box>
    </Stack>
  );
}
