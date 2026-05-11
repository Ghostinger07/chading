interface Props {
  size?: number;
}

/**
 * Lumen brand mark — a small stylized flame on an ink background.
 * Pure SVG so it scales crisp at any density.
 */
export default function BrandMark({ size = 28 }: Props) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 64 64"
      width={size}
      height={size}
      aria-label="Lumen"
      role="img"
      style={{ display: 'block', flexShrink: 0 }}
    >
      <defs>
        <linearGradient id="lm-bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#1A1824" />
          <stop offset="100%" stopColor="#0F0E13" />
        </linearGradient>
        <linearGradient id="lm-flame" x1="50%" y1="0%" x2="50%" y2="100%">
          <stop offset="0%" stopColor="#FEF3C7" />
          <stop offset="55%" stopColor="#FBBF24" />
          <stop offset="100%" stopColor="#B45309" />
        </linearGradient>
      </defs>
      <rect width="64" height="64" rx="14" fill="url(#lm-bg)" />
      <g transform="translate(22,14)">
        <path
          d="M10 2 C 5 11, 3 16, 6 22 C 8 19, 10 18, 12 20 C 10 14, 15 10, 15 4 C 18 9, 21 15, 20 22 C 19 31, 13 38, 6 36 C 0 34, -2 28, 2 23 C 4 27, 8 28, 9 25 C 6 21, 6 14, 10 2 Z"
          fill="url(#lm-flame)"
        />
        <circle cx="9" cy="26" r="2" fill="#FEF3C7" opacity="0.9" />
      </g>
    </svg>
  );
}
