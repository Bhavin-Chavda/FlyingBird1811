import React from 'react';

interface Props {
  size?: number;
  className?: string;
  /** Animate with a soaring / gliding motion */
  animated?: boolean;
}

/**
 * Consistent FlyingBird SVG logo used across the entire app.
 * viewBox 100 × 56 — two swept wings, oval body, forked tail.
 */
const FlyingBirdLogo: React.FC<Props> = ({ size = 28, className = '', animated = false }) => (
  <svg
    width={size}
    height={Math.round(size * 0.56)}
    viewBox="0 0 100 56"
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
    className={`fb-logo${animated ? ' fb-logo--soar' : ''}${className ? ` ${className}` : ''}`}
    aria-label="FlyingBird"
    role="img"
  >
    {/* ── Left wing ── */}
    <path d="M50 27 C38 14 20 7 2 14 C18 13 36 21 50 27Z" />

    {/* ── Right wing ── */}
    <path d="M50 27 C62 14 80 7 98 14 C82 13 64 21 50 27Z" />

    {/* ── Body ── */}
    <ellipse cx="50" cy="29.5" rx="4" ry="5.5" />

    {/* ── Forked tail ── */}
    <line
      x1="48" y1="34"
      x2="43" y2="46"
      stroke="currentColor" strokeWidth="2.8" strokeLinecap="round" fill="none"
    />
    <line
      x1="52" y1="34"
      x2="57" y2="46"
      stroke="currentColor" strokeWidth="2.8" strokeLinecap="round" fill="none"
    />
  </svg>
);

export default FlyingBirdLogo;
