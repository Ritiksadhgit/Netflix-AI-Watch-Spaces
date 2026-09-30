import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import SkeletonCard from '../common/SkeletonCard';
import { ShieldAlert } from 'lucide-react';
import CinematicButton from '../common/CinematicButton';

export default function ProtectedRoute({ children, requiredRole = null }) {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 space-y-6">
        <SkeletonCard height="h-64" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <SkeletonCard height="h-40" />
          <SkeletonCard height="h-40" />
          <SkeletonCard height="h-40" />
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />;
  }

  if (requiredRole && user.role !== requiredRole && user.role !== 'ADMIN') {
    return (
      <div className="min-h-[60vh] flex items-center justify-center px-4">
        <div className="glass-panel p-8 rounded-2xl max-w-md w-full text-center space-y-4 border-rose-500/30">
          <div className="w-14 h-14 mx-auto rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center text-rose-400">
            <ShieldAlert className="w-7 h-7" />
          </div>
          <h2 className="text-xl font-bold text-white">Access Restricted</h2>
          <p className="text-sm text-slate-400 leading-relaxed">
            This section requires <strong>{requiredRole}</strong> privileges. Your current role is <strong>{user.role}</strong>.
          </p>
          <CinematicButton
            variant="secondary"
            onClick={() => window.history.back()}
            className="w-full"
          >
            Go Back
          </CinematicButton>
        </div>
      </div>
    );
  }

  return children;
}
