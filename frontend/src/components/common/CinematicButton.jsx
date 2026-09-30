import React from 'react';
import { Loader2 } from 'lucide-react';

export default function CinematicButton({
  children,
  variant = 'primary', // primary, secondary, glass, danger, ghost
  size = 'md',        // sm, md, lg
  loading = false,
  disabled = false,
  className = '',
  onClick,
  type = 'button',
  ...props
}) {
  const baseStyles = 'relative inline-flex items-center justify-center font-medium rounded-xl transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-obsidian-950 disabled:opacity-50 disabled:cursor-not-allowed select-none active:scale-[0.98]';

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-5 py-2.5 text-sm',
    lg: 'px-7 py-3.5 text-base font-semibold',
  };

  const variantStyles = {
    primary: 'bg-gradient-to-r from-brand-indigo to-brand-purple text-white shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40 hover:brightness-110 focus:ring-brand-indigo border border-indigo-400/20',
    secondary: 'bg-obsidian-800 text-slate-200 hover:bg-obsidian-700 hover:text-white border border-slate-700/50 focus:ring-slate-500',
    glass: 'glass-panel text-slate-100 hover:bg-white/10 hover:border-white/20 focus:ring-brand-purple/50',
    danger: 'bg-gradient-to-r from-rose-600 to-rose-700 text-white shadow-lg shadow-rose-600/20 hover:shadow-rose-600/35 hover:brightness-110 focus:ring-rose-500 border border-rose-400/20',
    ghost: 'text-slate-400 hover:text-white hover:bg-white/5 focus:ring-white/20',
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin text-current" />}
      {children}
    </button>
  );
}
