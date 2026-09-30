import React from 'react';
import { Link } from 'react-router-dom';
import CinematicButton from '../components/common/CinematicButton';
import { Film, Home } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4 text-center">
      <div className="glass-panel p-10 rounded-3xl max-w-md w-full space-y-5 border-white/10 glow-indigo">
        <div className="w-16 h-16 mx-auto rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-brand-purple">
          <Film className="w-8 h-8" />
        </div>
        <div className="space-y-1">
          <h1 className="text-4xl font-extrabold text-white">404</h1>
          <h2 className="text-lg font-bold text-slate-200">Scene Not Found</h2>
        </div>
        <p className="text-xs text-slate-400 leading-relaxed">
          The requested Watch Space scene or URL does not exist or has already concluded.
        </p>
        <Link to="/" className="inline-block pt-2">
          <CinematicButton variant="primary" size="md">
            <Home className="w-4 h-4 mr-2" />
            <span>Return to Home</span>
          </CinematicButton>
        </Link>
      </div>
    </div>
  );
}
