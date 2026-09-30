import React, { useState, useEffect } from 'react';
import { analyticsService } from '../../services/analyticsService';
import GlassCard from '../common/GlassCard';
import CinematicButton from '../common/CinematicButton';
import { 
  BarChart3, 
  Clock, 
  Users, 
  MessageSquare, 
  Sparkles, 
  Award, 
  Vote, 
  RefreshCw, 
  X 
} from 'lucide-react';

export default function SessionAnalyticsModal({ isOpen, onClose, watchSpaceId, spaceName }) {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isReconciling, setIsReconciling] = useState(false);

  const fetchAnalytics = async () => {
    if (!watchSpaceId) return;
    try {
      setLoading(true);
      const data = await analyticsService.getSessionAnalytics(watchSpaceId);
      setAnalytics(data);
    } catch (err) {
      console.warn('Failed to load session analytics', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReconcile = async () => {
    if (!watchSpaceId) return;
    try {
      setIsReconciling(true);
      const data = await analyticsService.reconcileSessionAnalytics(watchSpaceId);
      setAnalytics(data);
    } catch (err) {
      console.warn('Failed to reconcile analytics', err);
    } finally {
      setIsReconciling(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchAnalytics();
    }
  }, [isOpen, watchSpaceId]);

  if (!isOpen) return null;

  const formatDuration = (secs) => {
    if (!secs) return '0 min';
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    if (m >= 60) {
      const h = (m / 60).toFixed(1);
      return `${h} hrs`;
    }
    return `${m}m ${s}s`;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-obsidian-950/85 backdrop-blur-md animate-fade-in">
      <GlassCard className="max-w-xl w-full p-6 sm:p-8 space-y-6 border-indigo-500/30 relative overflow-hidden shadow-2xl">
        {/* Glow decoration */}
        <div className="absolute -top-16 -right-16 w-48 h-48 bg-indigo-500/15 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-16 -left-16 w-48 h-48 bg-violet-500/15 rounded-full blur-3xl pointer-events-none" />

        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-white/10 pb-4 relative z-10">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-500/20 border border-indigo-500/40 flex items-center justify-center text-indigo-400">
              <BarChart3 className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">
                Watch Space Session Analytics
              </h3>
              <p className="text-xs text-slate-400 truncate max-w-xs sm:max-w-sm">
                {spaceName || watchSpaceId} &bull; Reconciled in MySQL
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-white/5 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Metrics Grid */}
        {loading ? (
          <div className="py-12 flex flex-col items-center justify-center space-y-3">
            <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin" />
            <span className="text-xs text-slate-400">Reconciling session data...</span>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3.5 relative z-10">
            {/* Duration */}
            <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-1">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400">
                <Clock className="w-3.5 h-3.5 text-indigo-400" />
                <span>Stream Duration</span>
              </div>
              <div className="text-xl font-bold text-white">
                {formatDuration(analytics?.sessionDurationSeconds)}
              </div>
            </div>

            {/* Peak Participants */}
            <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-1">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400">
                <Users className="w-3.5 h-3.5 text-emerald-400" />
                <span>Peak Viewers</span>
              </div>
              <div className="text-xl font-bold text-white">
                {analytics?.peakParticipants || 1}
              </div>
            </div>

            {/* Chat Messages */}
            <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-1">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400">
                <MessageSquare className="w-3.5 h-3.5 text-purple-400" />
                <span>Chat Messages</span>
              </div>
              <div className="text-xl font-bold text-white">
                {analytics?.chatMessagesCount || 0}
              </div>
            </div>

            {/* AI Questions */}
            <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-1">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400">
                <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                <span>AI Q&A Queries</span>
              </div>
              <div className="text-xl font-bold text-white">
                {analytics?.aiQuestionsCount || 0}
              </div>
            </div>

            {/* Trivia Triggered */}
            <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-1">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400">
                <Award className="w-3.5 h-3.5 text-amber-400" />
                <span>Trivia Events</span>
              </div>
              <div className="text-xl font-bold text-white">
                {analytics?.triviaShownCount || 0}
              </div>
            </div>

            {/* Narrative Votes */}
            <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-1">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400">
                <Vote className="w-3.5 h-3.5 text-rose-400" />
                <span>Votes Cast</span>
              </div>
              <div className="text-xl font-bold text-white">
                {analytics?.votesCastCount || 0}
              </div>
            </div>
          </div>
        )}

        {/* Modal Footer */}
        <div className="pt-2 flex items-center justify-between border-t border-white/10 relative z-10">
          <CinematicButton
            variant="glass"
            size="sm"
            onClick={handleReconcile}
            disabled={isReconciling}
            className="text-xs"
          >
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isReconciling ? 'animate-spin' : ''}`} />
            <span>{isReconciling ? 'Reconciling...' : 'Snapshot Analytics'}</span>
          </CinematicButton>

          <CinematicButton
            variant="primary"
            size="sm"
            onClick={onClose}
            className="text-xs"
          >
            Close
          </CinematicButton>
        </div>
      </GlassCard>
    </div>
  );
}
