import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Play, Sparkles, LogOut, LayoutDashboard, Shield, Film, History, User } from 'lucide-react';
import CinematicButton from './CinematicButton';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const navLinks = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'History', path: '/history', icon: History },
  ];

  return (
    <header className="sticky top-0 z-40 w-full backdrop-blur-xl bg-obsidian-950/80 border-b border-white/5 transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        
        {/* Brand Logo */}
        <Link to="/" className="flex items-center space-x-3 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-indigo to-brand-purple flex items-center justify-center shadow-lg shadow-indigo-500/25 group-hover:scale-105 transition-transform duration-200">
            <Play className="w-5 h-5 text-white fill-white ml-0.5" />
          </div>
          <div>
            <div className="flex items-center space-x-1.5">
              <span className="text-xl font-bold tracking-tight text-white font-sans">
                Watch<span className="bg-gradient-to-r from-brand-indigo to-brand-purple bg-clip-text text-transparent">Spaces</span>
              </span>
              <span className="text-[10px] font-semibold tracking-wider uppercase px-1.5 py-0.5 rounded-full bg-brand-purple/20 text-brand-purple border border-brand-purple/30">
                AI
              </span>
            </div>
            <p className="text-[10px] text-slate-400 tracking-wider uppercase font-medium">
              Synchronized Streaming
            </p>
          </div>
        </Link>

        {/* Center Nav Links (when authenticated) */}
        {isAuthenticated && (
          <nav className="hidden md:flex items-center space-x-1 glass-panel px-3 py-1.5 rounded-full">
            {navLinks.map((link) => {
              const Icon = link.icon;
              const isActive = location.pathname === link.path;
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`flex items-center space-x-2 px-4 py-2 rounded-full text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-gradient-to-r from-brand-indigo/30 to-brand-purple/30 text-white border border-indigo-500/30'
                      : 'text-slate-400 hover:text-white hover:bg-white/5'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{link.name}</span>
                </Link>
              );
            })}
          </nav>
        )}

        {/* Right CTA / User Profile */}
        <div className="flex items-center space-x-4">
          {isAuthenticated ? (
            <div className="flex items-center space-x-3">
              <Link
                to="/dashboard"
                className="hidden sm:inline-flex items-center space-x-2 text-xs font-semibold px-3 py-1.5 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 hover:bg-indigo-500/20 transition-colors"
              >
                <Sparkles className="w-3.5 h-3.5 text-brand-purple" />
                <span>AI Co-Pilot Active</span>
              </Link>

              {/* User Pill */}
              <div className="flex items-center space-x-2 pl-2 border-l border-white/10">
                <img
                  src={user.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}
                  alt={user.displayName}
                  className="w-9 h-9 rounded-full object-cover ring-2 ring-indigo-500/30"
                />
                <div className="hidden lg:block text-left">
                  <div className="text-xs font-semibold text-white leading-tight">
                    {user.displayName}
                  </div>
                  <div className="flex items-center space-x-1 mt-0.5">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-brand-purple">
                      {user.role}
                    </span>
                    {user.role === 'ADMIN' && <Shield className="w-3 h-3 text-amber-400" />}
                  </div>
                </div>

                <button
                  onClick={handleLogout}
                  title="Sign Out"
                  className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors ml-1"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </div>
          ) : (
            <div className="flex items-center space-x-3">
              <Link
                to="/login"
                className="text-sm font-medium text-slate-300 hover:text-white px-3 py-2 transition-colors"
              >
                Sign In
              </Link>
              <CinematicButton
                size="sm"
                onClick={() => navigate('/register')}
              >
                Get Started
              </CinematicButton>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
