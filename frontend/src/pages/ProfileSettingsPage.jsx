import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import GlassCard from '../components/common/GlassCard';
import CinematicButton from '../components/common/CinematicButton';
import {
  User,
  Shield,
  Sparkles,
  Sliders,
  Check,
  AlertCircle,
  Clock,
  KeyRound,
  Tv,
  Subtitles,
  Volume2,
  Gauge,
  LogOut,
  Save,
  Camera,
} from 'lucide-react';

const PRESET_AVATARS = [
  {
    id: 'avatar-1',
    label: 'Cyberpunk Pilot',
    url: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 'avatar-2',
    label: 'Neural Hacker',
    url: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 'avatar-3',
    label: 'Cosmic Navigator',
    url: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 'avatar-4',
    label: 'Deep Space Scout',
    url: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 'avatar-5',
    label: 'Synthetic Android',
    url: 'https://images.unsplash.com/photo-1628157582853-a796fa650a6a?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 'avatar-6',
    label: 'Quantum Engineer',
    url: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80',
  },
];

export default function ProfileSettingsPage() {
  const { user, updateProfile, logout } = useAuth();
  const { addToast } = useToast();

  const [displayName, setDisplayName] = useState(user?.displayName || '');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
  const [subtitleLocale, setSubtitleLocale] = useState(user?.subtitleLocale || 'en-US');
  const [customAvatarInput, setCustomAvatarInput] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  // Client-side UI preferences (saved to localStorage)
  const [aiVerbosity, setAiVerbosity] = useState(() => {
    return localStorage.getItem('app_pref_ai_verbosity') || 'NORMAL';
  });
  const [driftTolerance, setDriftTolerance] = useState(() => {
    return localStorage.getItem('app_pref_drift_tolerance') || '150';
  });
  const [soundEffects, setSoundEffects] = useState(() => {
    return localStorage.getItem('app_pref_sound_fx') !== 'false';
  });
  const [showTelemetry, setShowTelemetry] = useState(() => {
    return localStorage.getItem('app_pref_show_telemetry') === 'true';
  });

  useEffect(() => {
    if (user) {
      setDisplayName(user.displayName || '');
      setAvatarUrl(user.avatarUrl || '');
      setSubtitleLocale(user.subtitleLocale || 'en-US');
    }
  }, [user]);

  const handleSelectPreset = (url) => {
    setAvatarUrl(url);
    setCustomAvatarInput('');
  };

  const handleApplyCustomAvatar = (e) => {
    e.preventDefault();
    if (customAvatarInput.trim()) {
      setAvatarUrl(customAvatarInput.trim());
      setCustomAvatarInput('');
      addToast('Avatar URL preview updated. Click Save Changes to commit.', 'info');
    }
  };

  const handleSaveChanges = async (e) => {
    e.preventDefault();
    if (!displayName.trim() || displayName.length < 2) {
      addToast('Display name must be at least 2 characters.', 'error');
      return;
    }

    setIsSaving(true);
    try {
      // 1. Update backend user profile
      await updateProfile({
        displayName: displayName.trim(),
        avatarUrl: avatarUrl.trim(),
        subtitleLocale: subtitleLocale.trim(),
      });

      // 2. Persist local UI preferences
      localStorage.setItem('app_pref_ai_verbosity', aiVerbosity);
      localStorage.setItem('app_pref_drift_tolerance', driftTolerance);
      localStorage.setItem('app_pref_sound_fx', soundEffects.toString());
      localStorage.setItem('app_pref_show_telemetry', showTelemetry.toString());

      addToast('Profile and preferences updated successfully.', 'success');
    } catch (err) {
      addToast(err?.message || 'Failed to update profile settings.', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-obsidian-950 text-white pt-24 pb-20 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b border-white/5">
          <div>
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-indigo to-brand-purple flex items-center justify-center shadow-lg shadow-indigo-500/20">
                <Sliders className="w-5 h-5 text-white" />
              </div>
              <div>
                <h1 className="text-2xl sm:text-3xl font-bold font-sans text-white tracking-tight">
                  Profile & Studio Settings
                </h1>
                <p className="text-sm text-slate-400">
                  Manage your identity, synchronized streaming tolerances, and AI Co-Pilot preferences.
                </p>
              </div>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <CinematicButton
              variant="secondary"
              size="sm"
              icon={LogOut}
              onClick={logout}
            >
              Sign Out
            </CinematicButton>
            <CinematicButton
              variant="primary"
              size="sm"
              icon={isSaving ? Clock : Save}
              disabled={isSaving}
              onClick={handleSaveChanges}
            >
              {isSaving ? 'Saving...' : 'Save Changes'}
            </CinematicButton>
          </div>
        </div>

        {/* Identity & Account Card */}
        <GlassCard className="p-6 sm:p-8 space-y-6">
          <div className="flex items-center justify-between pb-4 border-b border-white/5">
            <h2 className="text-lg font-semibold text-white flex items-center space-x-2">
              <User className="w-5 h-5 text-brand-purple" />
              <span>Identity & Avatar</span>
            </h2>
            <div className="flex items-center space-x-2 px-3 py-1 rounded-full bg-white/5 border border-white/10 text-xs">
              <Shield className={`w-3.5 h-3.5 ${user?.role === 'ADMIN' ? 'text-amber-400' : 'text-brand-purple'}`} />
              <span className="font-semibold uppercase tracking-wider text-slate-300">
                Role: <span className="text-white">{user?.role || 'VIEWER'}</span>
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
            {/* Current Avatar Preview */}
            <div className="flex flex-col items-center p-6 rounded-2xl bg-white/[0.02] border border-white/5 space-y-3 text-center">
              <div className="relative group">
                <img
                  src={avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}
                  alt={displayName}
                  className="w-24 h-24 rounded-full object-cover ring-4 ring-brand-purple/30 shadow-xl shadow-brand-purple/10"
                />
                <div className="absolute inset-0 rounded-full bg-black/50 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                  <Camera className="w-6 h-6 text-white" />
                </div>
              </div>
              <div>
                <p className="text-sm font-semibold text-white">{displayName || 'Anonymous User'}</p>
                <p className="text-xs text-slate-400">{user?.email}</p>
              </div>
            </div>

            {/* Avatar Selectors */}
            <div className="md:col-span-2 space-y-4">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-400 block">
                Choose Preset Avatar
              </label>
              <div className="grid grid-cols-3 sm:grid-cols-6 gap-3">
                {PRESET_AVATARS.map((preset) => {
                  const isSelected = avatarUrl === preset.url;
                  return (
                    <button
                      key={preset.id}
                      type="button"
                      onClick={() => handleSelectPreset(preset.url)}
                      className={`relative rounded-xl overflow-hidden aspect-square border-2 transition-all p-0.5 ${
                        isSelected
                          ? 'border-brand-purple shadow-lg shadow-brand-purple/30 scale-105'
                          : 'border-white/10 hover:border-white/30 opacity-70 hover:opacity-100'
                      }`}
                    >
                      <img src={preset.url} alt={preset.label} className="w-full h-full object-cover rounded-lg" />
                      {isSelected && (
                        <div className="absolute top-1 right-1 w-4 h-4 rounded-full bg-brand-purple flex items-center justify-center">
                          <Check className="w-2.5 h-2.5 text-white" />
                        </div>
                      )}
                    </button>
                  );
                })}
              </div>

              {/* Custom Avatar URL Input */}
              <div className="pt-2">
                <label className="text-xs text-slate-400 block mb-1.5">
                  Or enter custom avatar image URL:
                </label>
                <div className="flex gap-2">
                  <input
                    type="url"
                    value={customAvatarInput}
                    onChange={(e) => setCustomAvatarInput(e.target.value)}
                    placeholder="https://example.com/my-photo.jpg"
                    className="flex-1 bg-obsidian-900/80 border border-white/10 rounded-xl px-3 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-brand-purple transition-colors"
                  />
                  <button
                    type="button"
                    onClick={handleApplyCustomAvatar}
                    className="px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-semibold text-slate-300 transition-colors"
                  >
                    Apply URL
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Form Fields */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
            <div>
              <label className="text-xs font-semibold text-slate-400 block mb-1.5">
                Display Name
              </label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                maxLength={50}
                className="w-full bg-obsidian-900/80 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-brand-purple transition-colors"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-400 block mb-1.5">
                Email Address (Account Credential)
              </label>
              <input
                type="email"
                value={user?.email || ''}
                disabled
                className="w-full bg-obsidian-900/40 border border-white/5 rounded-xl px-4 py-2.5 text-sm text-slate-500 cursor-not-allowed"
              />
            </div>
          </div>
        </GlassCard>

        {/* Playback & Real-Time Sync Tolerances */}
        <GlassCard className="p-6 sm:p-8 space-y-6">
          <div className="flex items-center space-x-2 pb-4 border-b border-white/5">
            <Gauge className="w-5 h-5 text-indigo-400" />
            <h2 className="text-lg font-semibold text-white">Playback & Synchronized Drift Controls</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Preferred Subtitle Locale */}
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-400 flex items-center space-x-2">
                <Subtitles className="w-4 h-4 text-brand-purple" />
                <span>Default Subtitle Track</span>
              </label>
              <p className="text-xs text-slate-400">
                Automatically activates localized subtitles when entering synchronized Watch Spaces.
              </p>
              <select
                value={subtitleLocale}
                onChange={(e) => setSubtitleLocale(e.target.value)}
                className="w-full bg-obsidian-900/80 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-brand-purple"
              >
                <option value="none">Off (No default subtitles)</option>
                <option value="en-US">English (CC - Full Closed Captions)</option>
                <option value="es-ES">Spanish (Español Neutro)</option>
                <option value="fr-FR">French (Français Sous-titres)</option>
              </select>
            </div>

            {/* Sync Drift Tolerance */}
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-400 flex items-center space-x-2">
                <Gauge className="w-4 h-4 text-emerald-400" />
                <span>Micro-Drift Tolerance Window</span>
              </label>
              <p className="text-xs text-slate-400">
                Configures client-side adaptive rate scaling (1.08x/0.92x) vs instant hard seeking.
              </p>
              <select
                value={driftTolerance}
                onChange={(e) => setDriftTolerance(e.target.value)}
                className="w-full bg-obsidian-900/80 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-brand-purple"
              >
                <option value="100">Tight (&lt; 100ms drift target - High-speed LAN/Fiber)</option>
                <option value="150">Balanced (&lt; 150ms drift target - Recommended)</option>
                <option value="250">Relaxed (&lt; 250ms drift target - High jitter/Cellular)</option>
              </select>
            </div>
          </div>

          <div className="pt-4 border-t border-white/5 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Real-time Telemetry Toggle */}
            <div className="flex items-center justify-between p-4 rounded-xl bg-white/[0.02] border border-white/5">
              <div>
                <span className="text-sm font-medium text-white block">Real-time Telemetry Overlay</span>
                <span className="text-xs text-slate-400">Display micro-drift ms, jitter and NTP round-trip</span>
              </div>
              <input
                type="checkbox"
                checked={showTelemetry}
                onChange={(e) => setShowTelemetry(e.target.checked)}
                className="w-4 h-4 rounded text-brand-indigo focus:ring-brand-indigo bg-obsidian-900 border-white/20 cursor-pointer"
              />
            </div>

            {/* Sound FX Toggle */}
            <div className="flex items-center justify-between p-4 rounded-xl bg-white/[0.02] border border-white/5">
              <div>
                <span className="text-sm font-medium text-white block">Trivia Sound Notifications</span>
                <span className="text-xs text-slate-400">Play subtle chime on synchronized trivia triggers</span>
              </div>
              <input
                type="checkbox"
                checked={soundEffects}
                onChange={(e) => setSoundEffects(e.target.checked)}
                className="w-4 h-4 rounded text-brand-indigo focus:ring-brand-indigo bg-obsidian-900 border-white/20 cursor-pointer"
              />
            </div>
          </div>
        </GlassCard>

        {/* AI Co-Pilot Preferences */}
        <GlassCard className="p-6 sm:p-8 space-y-6">
          <div className="flex items-center space-x-2 pb-4 border-b border-white/5">
            <Sparkles className="w-5 h-5 text-brand-purple" />
            <h2 className="text-lg font-semibold text-white">AI Co-Pilot Grounding Preferences</h2>
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-400 block mb-1.5">
                Default AI Verbosity Level
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {[
                  {
                    level: 'MINIMAL',
                    title: 'Minimal (Quick Facts)',
                    desc: 'Short, rapid bullet answers citing key timeline markers without spoiler details.',
                  },
                  {
                    level: 'NORMAL',
                    title: 'Normal (Contextual)',
                    desc: 'Balanced narrative explanations with scene context and primary character insights.',
                  },
                  {
                    level: 'CHATTY',
                    title: 'Chatty (Deep Dive)',
                    desc: 'Comprehensive trivia, cinematographic lore, and thematic background analysis.',
                  },
                ].map((item) => {
                  const isSelected = aiVerbosity === item.level;
                  return (
                    <button
                      key={item.level}
                      type="button"
                      onClick={() => setAiVerbosity(item.level)}
                      className={`text-left p-4 rounded-xl border transition-all ${
                        isSelected
                          ? 'bg-brand-purple/15 border-brand-purple text-white shadow-lg shadow-purple-500/10'
                          : 'bg-white/[0.02] border-white/5 text-slate-400 hover:border-white/20 hover:text-white'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs font-bold uppercase tracking-wider text-brand-purple">
                          {item.level}
                        </span>
                        {isSelected && <Check className="w-3.5 h-3.5 text-brand-purple" />}
                      </div>
                      <p className="text-sm font-semibold text-white mb-1">{item.title}</p>
                      <p className="text-xs text-slate-400 leading-relaxed">{item.desc}</p>
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        </GlassCard>

        {/* Security & Authentication Info */}
        <GlassCard className="p-6 sm:p-8 space-y-4">
          <div className="flex items-center space-x-2 pb-4 border-b border-white/5">
            <KeyRound className="w-5 h-5 text-emerald-400" />
            <h2 className="text-lg font-semibold text-white">Security & Active Session</h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
            <div className="p-4 rounded-xl bg-white/[0.02] border border-white/5">
              <span className="text-slate-400 block mb-1">Access Token Lifecycle</span>
              <span className="text-white font-medium">15-Minute Short-Lived JWT</span>
            </div>
            <div className="p-4 rounded-xl bg-white/[0.02] border border-white/5">
              <span className="text-slate-400 block mb-1">Refresh Mechanism</span>
              <span className="text-white font-medium">7-Day Rotated Cryptographic Token</span>
            </div>
            <div className="p-4 rounded-xl bg-white/[0.02] border border-white/5">
              <span className="text-slate-400 block mb-1">Role Authorization</span>
              <span className="text-emerald-400 font-semibold">{user?.role || 'VIEWER'} (Verified)</span>
            </div>
          </div>
        </GlassCard>

        {/* Bottom Save Bar */}
        <div className="flex items-center justify-end space-x-3 pt-4">
          <CinematicButton
            variant="primary"
            size="md"
            icon={isSaving ? Clock : Save}
            disabled={isSaving}
            onClick={handleSaveChanges}
          >
            {isSaving ? 'Saving Changes...' : 'Save All Settings'}
          </CinematicButton>
        </div>

      </div>
    </div>
  );
}
