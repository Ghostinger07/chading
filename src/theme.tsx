import { createContext, useContext, useMemo, useState, ReactNode, useEffect } from 'react';
import { createTheme, ThemeProvider } from '@mui/material/styles';

type Mode = 'light' | 'dark';

interface ThemeCtx {
  mode: Mode;
  toggle: () => void;
}

const Ctx = createContext<ThemeCtx>({ mode: 'light', toggle: () => {} });
export const useThemeMode = () => useContext(Ctx);

const STORAGE_KEY = 'chading.themeMode';

export function AppThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<Mode>(() => {
    const saved = localStorage.getItem(STORAGE_KEY) as Mode | null;
    if (saved === 'light' || saved === 'dark') return saved;
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, mode);
  }, [mode]);

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode,
          primary: { main: mode === 'dark' ? '#bb86fc' : '#6750a4' },
          secondary: { main: '#03dac6' },
          background: {
            default: mode === 'dark' ? '#121212' : '#f6f4fb',
            paper: mode === 'dark' ? '#1e1e1e' : '#ffffff',
          },
        },
        shape: { borderRadius: 14 },
        typography: {
          fontFamily:
            'Roboto, "Noto Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
          h5: { fontWeight: 600 },
          h6: { fontWeight: 600 },
        },
        components: {
          MuiPaper: { styleOverrides: { root: { backgroundImage: 'none' } } },
          MuiButton: { defaultProps: { disableElevation: true } },
        },
      }),
    [mode],
  );

  const value = useMemo<ThemeCtx>(
    () => ({ mode, toggle: () => setMode((m) => (m === 'dark' ? 'light' : 'dark')) }),
    [mode],
  );

  return (
    <Ctx.Provider value={value}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </Ctx.Provider>
  );
}
