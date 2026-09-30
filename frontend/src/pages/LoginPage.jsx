import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import CinematicButton from '../components/common/CinematicButton';
import GlassCard from '../components/common/GlassCard';
import { Lock, Mail, Play, AlertCircle, Sparkles, UserCheck } from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Redirect destination after successful login
  const from = new URLSearchParams(location.search).get('redirect') || '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login(email, password);
      toast.success('Welcome back to WatchSpaces AI!', 'Signed In');
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.detail || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError(null);
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center px-4 py-12">
      <div className="max-w-md w-full space-y-6">
        
        {/* Top Logo */}
        <div className="text-center space-y-2">
          <Link to="/" className="inline-flex items-center space-x-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-indigo to-brand-purple flex items-center justify-center shadow-lg shadow-indigo-500/25">
              <Play className="w-5 h-5 text-white fill-white ml-0.5" />
            </div>
            <span className="text-2xl font-bold tracking-tight text-white">
              Watch<span className="text-brand-purple">Spaces</span> AI
            </span>
          </Link>
          <h2 className="text-xl font-bold text-white tracking-tight pt-2">
            Sign In to Watch Space
          </h2>
          <p className="text-xs text-slate-400">
            Enter your credentials or choose a pre-configured demo account below
          </p>
        </div>

        {/* Form Card */}
        <GlassCard className="border-white/10 glow-indigo">
          <form onSubmit={handleSubmit} className="space-y-4">
            
            {/* Error Banner */}
            {error && (
              <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-start space-x-2 animate-shake">
                <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5 text-rose-400" />
                <span className="leading-relaxed">{error}</span>
              </div>
            )}

            {/* Email Input */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="viewer@netflixspaces.com"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-obsidian-900/80 border border-slate-700/60 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-brand-purple focus:ring-1 focus:ring-brand-purple transition-all"
                />
              </div>
            </div>

            {/* Password Input */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-obsidian-900/80 border border-slate-700/60 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-brand-purple focus:ring-1 focus:ring-brand-purple transition-all"
                />
              </div>
            </div>

            {/* Submit Button */}
            <CinematicButton
              type="submit"
              loading={loading}
              className="w-full py-3 mt-2 text-sm font-semibold"
            >
              Sign In
            </CinematicButton>
          </form>

          {/* Demo Quick-Fill Section */}
          <div className="mt-6 pt-6 border-t border-white/5 space-y-3">
            <div className="flex items-center space-x-1.5 text-xs font-semibold text-slate-400">
              <Sparkles className="w-3.5 h-3.5 text-brand-purple" />
              <span>One-Click Demo Accounts</span>
            </div>

            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickFill('viewer@netflixspaces.com', 'Password123!')}
                className="px-2.5 py-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-center transition-colors group"
              >
                <div className="text-[11px] font-bold text-white group-hover:text-brand-purple">Viewer</div>
                <div className="text-[9px] text-slate-400">Alex</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('host@netflixspaces.com', 'Password123!')}
                className="px-2.5 py-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-center transition-colors group"
              >
                <div className="text-[11px] font-bold text-emerald-400">Party Host</div>
                <div className="text-[9px] text-slate-400">Elena</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('admin@netflixspaces.com', 'Password123!')}
                className="px-2.5 py-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-center transition-colors group"
              >
                <div className="text-[11px] font-bold text-amber-400">Admin</div>
                <div className="text-[9px] text-slate-400">Chief</div>
              </button>
            </div>
          </div>
        </GlassCard>

        {/* Footer Link */}
        <p className="text-center text-xs text-slate-400">
          Don't have an account yet?{' '}
          <Link to="/register" className="font-semibold text-brand-purple hover:text-indigo-300">
            Create an account
          </Link>
        </p>

      </div>
    </div>
  );
}
