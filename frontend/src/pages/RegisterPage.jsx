import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import CinematicButton from '../components/common/CinematicButton';
import GlassCard from '../components/common/GlassCard';
import { Lock, Mail, User, Play, AlertCircle, Check, Shield } from 'lucide-react';

const AVATAR_PRESETS = [
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80',
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80',
  'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80',
];

export default function RegisterPage() {
  const { register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('VIEWER');
  const [avatarUrl, setAvatarUrl] = useState(AVATAR_PRESETS[0]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const getPasswordStrength = () => {
    if (!password) return 0;
    let score = 0;
    if (password.length >= 6) score += 1;
    if (password.length >= 10) score += 1;
    if (/[A-Z]/.test(password)) score += 1;
    if (/[0-9]/.test(password) || /[^A-Za-z0-9]/.test(password)) score += 1;
    return score;
  };

  const strength = getPasswordStrength();
  const strengthColors = ['bg-slate-700', 'bg-rose-500', 'bg-amber-500', 'bg-indigo-400', 'bg-emerald-400'];
  const strengthLabels = ['Too weak', 'Weak', 'Fair', 'Strong', 'Excellent'];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await register({
        displayName,
        email,
        password,
        role,
        avatarUrl,
      });
      toast.success('Account created! Welcome to WatchSpaces AI.', 'Registration Complete');
      navigate('/dashboard');
    } catch (err) {
      if (err.validationErrors) {
        const firstError = Object.values(err.validationErrors)[0];
        setError(firstError);
      } else {
        setError(err.detail || 'Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
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
            Create Your Account
          </h2>
          <p className="text-xs text-slate-400">
            Join public watch rooms, engage with AI scene trivia, and stream together
          </p>
        </div>

        {/* Form Card */}
        <GlassCard className="border-white/10 glow-purple">
          <form onSubmit={handleSubmit} className="space-y-4">
            
            {/* Error Alert */}
            {error && (
              <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-start space-x-2 animate-shake">
                <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5 text-rose-400" />
                <span className="leading-relaxed">{error}</span>
              </div>
            )}

            {/* Avatar Selector */}
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                Choose Profile Avatar
              </label>
              <div className="flex items-center space-x-3 justify-center py-1">
                {AVATAR_PRESETS.map((url, i) => (
                  <button
                    key={i}
                    type="button"
                    onClick={() => setAvatarUrl(url)}
                    className={`relative rounded-full transition-all duration-200 ${
                      avatarUrl === url
                        ? 'ring-2 ring-brand-purple ring-offset-2 ring-offset-obsidian-900 scale-110'
                        : 'opacity-60 hover:opacity-100'
                    }`}
                  >
                    <img
                      src={url}
                      alt={`Preset ${i}`}
                      className="w-10 h-10 rounded-full object-cover"
                    />
                    {avatarUrl === url && (
                      <div className="absolute inset-0 bg-brand-purple/20 rounded-full flex items-center justify-center">
                        <Check className="w-3.5 h-3.5 text-white" />
                      </div>
                    )}
                  </button>
                ))}
              </div>
            </div>

            {/* Display Name Input */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                Display Name
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <User className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  placeholder="Elena R."
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-obsidian-900/80 border border-slate-700/60 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-brand-purple focus:ring-1 focus:ring-brand-purple transition-all"
                />
              </div>
            </div>

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
                  placeholder="elena@example.com"
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
                  placeholder="At least 6 characters"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-obsidian-900/80 border border-slate-700/60 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-brand-purple focus:ring-1 focus:ring-brand-purple transition-all"
                />
              </div>

              {/* Password Strength Meter */}
              {password && (
                <div className="space-y-1 pt-1">
                  <div className="flex justify-between text-[10px] text-slate-400">
                    <span>Password Strength</span>
                    <span className="font-semibold text-slate-300">{strengthLabels[strength]}</span>
                  </div>
                  <div className="h-1.5 w-full bg-obsidian-900 rounded-full overflow-hidden flex">
                    <div
                      className={`h-full transition-all duration-300 ${strengthColors[strength]}`}
                      style={{ width: `${(strength / 4) * 100}%` }}
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Role Selection */}
            <div className="space-y-1.5 pt-1">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                Default Account Role
              </label>
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setRole('VIEWER')}
                  className={`p-3 rounded-xl border text-left transition-all ${
                    role === 'VIEWER'
                      ? 'bg-indigo-500/20 border-brand-indigo text-white ring-1 ring-brand-indigo'
                      : 'bg-obsidian-900/60 border-slate-700/60 text-slate-400 hover:border-slate-600'
                  }`}
                >
                  <div className="text-xs font-bold">Viewer</div>
                  <div className="text-[10px] opacity-75 mt-0.5">Join & watch spaces</div>
                </button>

                <button
                  type="button"
                  onClick={() => setRole('HOST')}
                  className={`p-3 rounded-xl border text-left transition-all ${
                    role === 'HOST'
                      ? 'bg-purple-500/20 border-brand-purple text-white ring-1 ring-brand-purple'
                      : 'bg-obsidian-900/60 border-slate-700/60 text-slate-400 hover:border-slate-600'
                  }`}
                >
                  <div className="text-xs font-bold text-emerald-400">Party Host</div>
                  <div className="text-[10px] opacity-75 mt-0.5">Create & moderate spaces</div>
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <CinematicButton
              type="submit"
              loading={loading}
              className="w-full py-3 mt-4 text-sm font-semibold glow-indigo"
            >
              Complete Registration
            </CinematicButton>
          </form>
        </GlassCard>

        {/* Footer Link */}
        <p className="text-center text-xs text-slate-400">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-brand-purple hover:text-indigo-300">
            Sign In
          </Link>
        </p>

      </div>
    </div>
  );
}
