import React from 'react';

export default function SkeletonCard({ className = '', height = 'h-48' }) {
  return (
    <div
      className={`glass-panel rounded-2xl overflow-hidden relative ${height} ${className}`}
    >
      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/5 to-transparent animate-shimmer" />
      <div className="p-5 flex flex-col justify-end h-full space-y-3">
        <div className="h-4 bg-white/10 rounded-md w-3/4 animate-pulse" />
        <div className="h-3 bg-white/5 rounded-md w-1/2 animate-pulse" />
      </div>
    </div>
  );
}
