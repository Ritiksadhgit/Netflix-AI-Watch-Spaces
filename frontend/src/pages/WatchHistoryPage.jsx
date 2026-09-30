import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { interactionService } from '../services/interactionService';
import { watchSpaceService } from '../services/watchSpaceService';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import GlassCard from '../components/common/GlassCard';
import CinematicButton from '../components/common/CinematicButton';
import CreateWatchSpaceModal from '../components/watchspace/CreateWatchSpaceModal';
import { 
  History, 
  Play, 
  CheckCircle2, 
  Clock, 
  Star, 
  Film, 
  Sparkles, 
  PlusCircle, 
  ChevronRight 
} from 'lucide-react';

export default function WatchHistoryPage() {
  const { user } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL'); // 'ALL' | 'IN_PROGRESS' | 'COMPLETED'
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [preselectedTitleId, setPreselectedTitleId] = useState(null);

  useEffect(() => {
    setLoading(true);
    interactionService.getWatchHistory()
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          setHistory(data);
        } else {
          // Provide realistic seeded fallback for demonstration
          setHistory([
            {
              id: 1,
              titleId: 1,
              titleName: 'Tears of Steel',
              posterUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80',
              backdropUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1920&q=80',
              watchedSeconds: 700,
              durationSeconds: 734,
              progressPercent: 95,
              completed: true,
              rating: 5,
              updatedAt: '2026-09-30T10:15:00',
            },
            {
              id: 2,
              titleId: 2,
              titleName: 'Sintel',
              posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
              backdropUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1920&q=80',
              watchedSeconds: 400,
              durationSeconds: 888,
              progressPercent: 45,
              completed: false,
              rating: 4,
              updatedAt: '2026-09-29T18:30:00',
            },
          ]);
        }
      })
      .catch((err) => {
        console.warn('Failed to load history', err);
      })
      .finally(() => setLoading(false));
  }, []);

  const filteredHistory = history.filter((item) => {
    if (filter === 'IN_PROGRESS') return !item.completed;
    if (filter === 'COMPLETED') return item.completed;
    return true;
  });

  const totalWatchedMinutes = Math.floor(
    history.reduce((sum, item) => sum + (item.watchedSeconds || 0), 0) / 60
  );
  const completedCount = history.filter((item) => item.completed).length;

  const handleStartSpace = (titleId) => {
    setPreselectedTitleId(titleId);
    setShowCreateModal(true);
  };

  const handleSpaceCreated = (newSpace) => {
    navigate(`/watch/${newSpace.id}`);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-10">
      {/* Header Banner */}
      <div className="relative rounded-3xl p-8 sm:p-10 glass-panel border-white/10 overflow-hidden glow-indigo">
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <div className="w-14 h-14 rounded-2xl bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shadow-xl">
              <History className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  Watch History & Activity
                </h1>
                <span className="text-xs px-2.5 py-0.5 rounded-full bg-white/10 text-slate-300 font-semibold">
                  {history.length} Titles
                </span>
              </div>
              <p className="text-xs sm:text-sm text-slate-300 mt-1">
                Your synchronized streams, playback progress, and party milestones
              </p>
            </div>
          </div>

          {/* Quick Metrics */}
          <div className="flex items-center space-x-4">
            <div className="text-center px-4 py-2 rounded-xl bg-white/5 border border-white/10">
              <div className="text-xl font-bold text-white">{totalWatchedMinutes}m</div>
              <div className="text-[11px] text-slate-400">Streamed</div>
            </div>
            <div className="text-center px-4 py-2 rounded-xl bg-white/5 border border-white/10">
              <div className="text-xl font-bold text-emerald-400">{completedCount}</div>
              <div className="text-[11px] text-slate-400">Completed</div>
            </div>
          </div>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center space-x-2 border-b border-white/10 pb-4">
        <button
          onClick={() => setFilter('ALL')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-colors ${
            filter === 'ALL'
              ? 'bg-indigo-600 text-white shadow-md'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          All Activity ({history.length})
        </button>
        <button
          onClick={() => setFilter('IN_PROGRESS')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-colors ${
            filter === 'IN_PROGRESS'
              ? 'bg-indigo-600 text-white shadow-md'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          In Progress ({history.filter((i) => !i.completed).length})
        </button>
        <button
          onClick={() => setFilter('COMPLETED')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-colors ${
            filter === 'COMPLETED'
              ? 'bg-indigo-600 text-white shadow-md'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          Completed ({completedCount})
        </button>
      </div>

      {/* History Items Grid */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center space-y-3">
          <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-xs text-slate-400">Loading your watch history...</span>
        </div>
      ) : filteredHistory.length === 0 ? (
        <GlassCard className="p-12 text-center space-y-4 max-w-lg mx-auto border-white/10">
          <Film className="w-12 h-12 text-slate-500 mx-auto" />
          <h3 className="text-lg font-bold text-white">No Watch History Found</h3>
          <p className="text-xs text-slate-400">
            You haven't watched any titles in this category yet. Explore recommendations or join an active Watch Space.
          </p>
          <CinematicButton
            variant="primary"
            size="md"
            onClick={() => navigate('/dashboard')}
          >
            Explore Watch Spaces
          </CinematicButton>
        </GlassCard>
      ) : (
        <div className="space-y-4">
          {filteredHistory.map((item) => (
            <GlassCard
              key={item.id}
              hoverEffect
              className="p-4 sm:p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-white/10"
            >
              <div className="flex items-center space-x-4 min-w-0">
                <div className="relative w-28 h-20 rounded-xl overflow-hidden shrink-0">
                  <img
                    src={item.posterUrl}
                    alt={item.titleName}
                    className="w-full h-full object-cover"
                  />
                  {/* Progress overlay bar */}
                  <div className="absolute bottom-0 left-0 right-0 h-1.5 bg-black/60">
                    <div
                      className="h-full bg-gradient-to-r from-indigo-500 to-brand-purple"
                      style={{ width: `${item.progressPercent}%` }}
                    />
                  </div>
                </div>

                <div className="space-y-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <h3 className="text-base font-bold text-white truncate">
                      {item.titleName}
                    </h3>
                    {item.completed ? (
                      <span className="inline-flex items-center text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                        <CheckCircle2 className="w-3 h-3 mr-1" /> Completed
                      </span>
                    ) : (
                      <span className="inline-flex items-center text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30">
                        <Clock className="w-3 h-3 mr-1" /> {item.progressPercent}% Watched
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-slate-400">
                    {Math.floor(item.watchedSeconds / 60)}m of {Math.floor(item.durationSeconds / 60)}m &bull; Last streamed {new Date(item.updatedAt || Date.now()).toLocaleDateString()}
                  </p>

                  {/* Rating Stars */}
                  {item.rating && (
                    <div className="flex items-center space-x-1 pt-0.5">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star
                          key={star}
                          className={`w-3.5 h-3.5 ${
                            star <= item.rating
                              ? 'text-amber-400 fill-amber-400'
                              : 'text-slate-600'
                          }`}
                        />
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center space-x-3 w-full sm:w-auto shrink-0 justify-end">
                <CinematicButton
                  variant="primary"
                  size="sm"
                  onClick={() => handleStartSpace(item.titleId)}
                  className="text-xs"
                >
                  <PlusCircle className="w-3.5 h-3.5 mr-1.5" />
                  <span>Host Watch Space</span>
                </CinematicButton>
              </div>
            </GlassCard>
          ))}
        </div>
      )}

      {/* Create Watch Space Modal */}
      <CreateWatchSpaceModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleSpaceCreated}
        preselectedTitleId={preselectedTitleId}
      />
    </div>
  );
}
