import React from 'react';

export default function GlassCard({
  children,
  className = '',
  hoverEffect = false,
  glow = null, // 'indigo', 'purple', 'emerald', null
  ...props
}) {
  const glowStyles = {
    indigo: 'glow-indigo border-indigo-500/30',
    purple: 'glow-purple border-brand-purple/30',
    emerald: 'glow-emerald border-emerald-500/30',
  };

  const selectedGlow = glow ? glowStyles[glow] : '';

  return (
    <div
      className={`glass-panel rounded-2xl p-6 transition-all duration-300 ${
        hoverEffect ? 'glass-panel-hover hover:-translate-y-1' : ''
      } ${selectedGlow} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
