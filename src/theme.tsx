import { createContext, useContext, useMemo, useState, ReactNode, useEffect } from 'react';
import { createTheme, ThemeProvider, alpha } from '@mui/material/styles';

type Mode = 'light' | 'dark';

interface ThemeCtx {
  mode: Mode;
  toggle: () => void;
}

const Ctx = createContext<ThemeCtx>({ mode: 'dark', toggle: () => {} });
export const useThemeMode = () => useContext(Ctx);

const STORAGE_KEY = 'lumen.themeMode';

// Lumen palette: warm amber light on midnight ink.
const AMBER = '#FBBF24';
const AMBER_DEEP = '#D97706';
const CREAM = '#FEF3C7';
const INK = '#0F0E13';
const INK_SOFT = '#1A1824';
const INK_PAPER = '#15131E';

const PARCHMENT_BG = '#FAF6EC';
const PARCHMENT_PAPER = '#FFFDF6';

export function AppThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<Mode>(() => {
    const saved = localStorage.getItem(STORAGE_KEY) as Mode | null;
    if (saved === 'light' || saved === 'dark') return saved;
    // Default to dark — it's the signature look.
    return 'dark';
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, mode);
  }, [mode]);

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode,
          primary: {
            main: mode === 'dark' ? AMBER : AMBER_DEEP,
            light: CREAM,
            dark: AMBER_DEEP,
            contrastText: mode === 'dark' ? INK : '#fff',
          },
          secondary: {
            main: mode === 'dark' ? CREAM : '#7C5A1F',
          },
          background: {
            default: mode === 'dark' ? INK : PARCHMENT_BG,
            paper: mode === 'dark' ? INK_PAPER : PARCHMENT_PAPER,
          },
          text: {
            primary: mode === 'dark' ? '#F5EFE0' : '#2A2318',
            secondary: mode === 'dark' ? alpha('#F5EFE0', 0.68) : alpha('#2A2318', 0.68),
          },
          divider: mode === 'dark' ? alpha(AMBER, 0.16) : alpha('#2A2318', 0.12),
        },
        shape: { borderRadius: 14 },
        typography: {
          fontFamily:
            '"Inter", "Noto Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
          h1: { fontFamily: '"Cormorant Garamond", Georgia, serif', fontWeight: 500 },
          h2: { fontFamily: '"Cormorant Garamond", Georgia, serif', fontWeight: 500 },
          h3: { fontFamily: '"Cormorant Garamond", Georgia, serif', fontWeight: 500 },
          h4: { fontFamily: '"Cormorant Garamond", Georgia, serif', fontWeight: 500 },
          h5: { fontFamily: '"Cormorant Garamond", Georgia, serif', fontWeight: 500, letterSpacing: 0.2 },
          h6: { fontWeight: 600, letterSpacing: 0.3 },
          overline: { letterSpacing: 2, fontWeight: 600 },
          button: { textTransform: 'none', fontWeight: 500, letterSpacing: 0.2 },
        },
        components: {
          MuiCssBaseline: {
            styleOverrides: {
              body: {
                backgroundImage:
                  mode === 'dark'
                    ? `radial-gradient(1200px 600px at 15% -10%, ${alpha(
                        AMBER,
                        0.08,
                      )}, transparent 60%), radial-gradient(900px 500px at 110% 110%, ${alpha(
                        AMBER_DEEP,
                        0.07,
                      )}, transparent 60%)`
                    : `radial-gradient(1000px 500px at 10% -10%, ${alpha(
                        AMBER,
                        0.18,
                      )}, transparent 60%)`,
                backgroundAttachment: 'fixed',
              },
              '::selection': {
                backgroundColor: alpha(AMBER, 0.35),
                color: mode === 'dark' ? CREAM : INK,
              },
            },
          },
          MuiPaper: {
            styleOverrides: {
              root: {
                backgroundImage: 'none',
                borderColor:
                  mode === 'dark' ? alpha(AMBER, 0.14) : alpha('#2A2318', 0.1),
              },
            },
          },
          MuiAppBar: {
            styleOverrides: {
              root: {
                backgroundColor:
                  mode === 'dark'
                    ? alpha(INK_SOFT, 0.72)
                    : alpha(PARCHMENT_PAPER, 0.82),
                backdropFilter: 'saturate(140%) blur(12px)',
                borderBottom: `1px solid ${
                  mode === 'dark' ? alpha(AMBER, 0.12) : alpha('#2A2318', 0.08)
                }`,
              },
            },
          },
          MuiButton: {
            defaultProps: { disableElevation: true },
            styleOverrides: {
              root: { borderRadius: 10 },
              containedPrimary: {
                boxShadow:
                  mode === 'dark'
                    ? `0 6px 24px ${alpha(AMBER, 0.28)}`
                    : `0 6px 20px ${alpha(AMBER_DEEP, 0.22)}`,
              },
            },
          },
          MuiTextField: { defaultProps: { variant: 'outlined' } },
          MuiOutlinedInput: {
            styleOverrides: {
              root: {
                backgroundColor:
                  mode === 'dark' ? alpha('#ffffff', 0.02) : alpha('#ffffff', 0.5),
              },
            },
          },
          MuiListItemButton: {
            styleOverrides: {
              root: {
                borderRadius: 10,
                '&.Mui-selected': {
                  backgroundColor: alpha(AMBER, mode === 'dark' ? 0.14 : 0.18),
                  '&:hover': {
                    backgroundColor: alpha(AMBER, mode === 'dark' ? 0.2 : 0.26),
                  },
                },
              },
            },
          },
          MuiTooltip: {
            styleOverrides: {
              tooltip: { fontSize: 12, fontWeight: 500 },
            },
          },
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
