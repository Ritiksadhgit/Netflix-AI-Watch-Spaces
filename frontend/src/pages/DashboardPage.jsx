import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { watchSpaceService } from '../services/watchSpaceService';
import CreateWatchSpaceModal from '../components/watchspace/CreateWatchSpaceModal';
import GlassCard from '../components/common/GlassCard';
import CinematicButton from '../components/common/CinematicButton';
import { 
  Play, 
  Users, 
  Sparkles, 
  PlusCircle, 
  KeyRound, 
  Clock, 
  Film, 
  Award,
  Radio
} from 'lucide-react';

export default function DashboardPage() {
  const { user } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [activeSpaces, setActiveSpaces] = useState([]);
  const [loadingSpaces, setLoadingSpaces] = useState(true);
  const [inviteCodeInput, setInviteCodeInput] = useState('');
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isJoining, setIsJoining] = useState(false);

  // Fallback sample spaces matching seeded DB records
  const sampleSpaces = [
    {
      id: 'ws_demo_live',
      name: 'Cyberpunk Sci-Fi Premiere Watch',
      titleName: 'Tears of Steel',
      posterUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80',
      participantsCount: 8,
      status: 'LIVE',
      inviteCode: 'CYBER-2026',
      hostName: 'Elena (Party Host)',
    },
    {
      id: 'ws_demo_sintel',
      name: 'Fantasy Quest Friday',
      titleName: 'Sintel',
      posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
      participantsCount: 4,
      status: 'SCHEDULED',
      inviteCode: 'QUEST-7712',
      hostName: 'Elena (Party Host)',
    }
  ];

  // Fetch active spaces from backend API
  useEffect(() => {
    setLoadingSpaces(true);
    watchSpaceService.getActiveSpaces()
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          const mapped = data.map((ws) => ({
            id: ws.id,
            name: ws.name,
            titleName: ws.title?.name || 'Feature Title',
            posterUrl: ws.title?.posterUrl || 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80',
            participantsCount: ws.currentParticipantsCount || 1,
            status: ws.status || 'LIVE',
            inviteCode: ws.inviteCode,
            hostName: ws.hostUser?.displayName || 'Host',
          }));
          setActiveSpaces(mapped);
        } else {
          setActiveSpaces(sampleSpaces);
        }
      })
      .catch((err) => {
        console.warn('Using seeded spaces fallback', err);
        setActiveSpaces(sampleSpaces);
      })
      .finally(() => setLoadingSpaces(false));
  }, []);

  const continueWatching = [
    {
      id: 1,
      name: 'Tears of Steel',
      progress: 65,
      duration: '12 min',
      genre: 'Sci-Fi, Cyberpunk',
      posterUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80',
      spaceId: 'ws_demo_live',
    },
    {
      id: 2,
      name: 'Sintel',
      progress: 30,
      duration: '15 min',
      genre: 'Fantasy, Adventure',
      posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
      spaceId: 'ws_demo_sintel',
    },
    {
      id: 3,
      name: 'Big Buck Bunny',
      progress: 10,
      duration: '10 min',
      genre: 'Animation, Comedy',
      posterUrl: 'https://images.unsplash.com/photo-1535083783855-76ae62b2914e?auto=format&fit=crop&w=600&q=80',
      spaceId: 'ws_demo_live',
    },
  ];

  const handleJoinByCode = async (e) => {
    e.preventDefault();
    if (!inviteCodeInput.trim()) return;

    try {
      setIsJoining(true);
      const code = inviteCodeInput.trim().toUpperCase();
      const res = await watchSpaceService.joinByCode(code);
      addToast(`Joined space: ${res.name}`, 'success');
      setShowJoinModal(false);
      navigate(`/watch/${res.id}`);
    } catch (err) {
      // If code matches sample fallback
      const code = inviteCodeInput.trim().toUpperCase();
      if (code === 'CYBER-2026') {
        addToast('Entering Cyberpunk Sci-Fi Premiere Watch space...', 'success');
        setShowJoinModal(false);
        navigate('/watch/ws_demo_live');
      } else if (code === 'QUEST-7712') {
        addToast('Entering Fantasy Quest Friday space...', 'success');
        setShowJoinModal(false);
        navigate('/watch/ws_demo_sintel');
      } else {
        addToast(err.message || 'Watch Space invite code not found or expired', 'error');
      }
    } finally {
      setIsJoining(false);
    }
  };

  const handleCreatedSpace = (newSpace) => {
    navigate(`/watch/${newSpace.id}`);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-12">
      
      {/* Welcome Banner */}
      <div className="relative rounded-3xl p-8 sm:p-10 glass-panel border-white/10 overflow-hidden glow-indigo">
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <img
              src={user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}
              alt={user?.displayName}
              className="w-16 h-16 rounded-2xl object-cover ring-2 ring-brand-purple/40 shadow-xl"
            />
            <div className="space-y-1">
              <div className="flex items-center space-x-2">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  Welcome back, {user?.displayName}
                </h1>
                <span className="text-xs font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-brand-purple/20 text-brand-purple border border-brand-purple/30">
                  {user?.role}
                </span>
              </div>
              <p className="text-xs sm:text-sm text-slate-300">
                Synchronized playback engine active &bull; AI Co-Pilot standby
              </p>
            </div>
          </div>

          {/* Quick Action Buttons */}
          <div className="flex items-center space-x-3 w-full md:w-auto">
            <CinematicButton
              variant="primary"
              size="md"
              onClick={() => setShowCreateModal(true)}
              className="flex-1 md:flex-initial"
            >
              <PlusCircle className="w-4 h-4 mr-2" />
              <span>Create Watch Space</span>
            </CinematicButton>

            <CinematicButton
              variant="glass"
              size="md"
              onClick={() => setShowJoinModal(true)}
              className="flex-1 md:flex-initial"
            >
              <KeyRound className="w-4 h-4 mr-2 text-brand-purple" />
              <span>Join with Code</span>
            </CinematicButton>
          </div>
        </div>
      </div>

      {/* Quick Stats Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <GlassCard className="p-5 border-white/5 flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-brand-indigo">
            <Radio className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-white">{activeSpaces.length} Live</div>
            <div className="text-xs text-slate-400">Available Spaces</div>
          </div>
        </GlassCard>

        <GlassCard className="p-5 border-white/5 flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-brand-purple">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-white">18.4 hrs</div>
            <div className="text-xs text-slate-400">Watched Together</div>
          </div>
        </GlassCard>

        <GlassCard className="p-5 border-white/5 flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Award className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-white">94%</div>
            <div className="text-xs text-slate-400">Trivia Accuracy</div>
          </div>
        </GlassCard>

        <GlassCard className="p-5 border-white/5 flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
            <Sparkles className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-white">42 Questions</div>
            <div className="text-xs text-slate-400">AI Co-Pilot Answers</div>
          </div>
        </GlassCard>
      </div>

      {/* Active Watch Spaces Rail */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Radio className="w-5 h-5 text-emerald-400 animate-pulse" />
            <h2 className="text-xl font-bold text-white tracking-tight">
              Active Watch Spaces Right Now
            </h2>
          </div>
          <span className="text-xs text-slate-400">
            Click to enter synchronized space
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {activeSpaces.map((space) => (
            <GlassCard
              key={space.id}
              hoverEffect
              className="p-5 flex items-center space-x-4 border-white/5 cursor-pointer group"
              onClick={() => navigate(`/watch/${space.id}`)}
            >
              <img
                src={space.posterUrl}
                alt={space.titleName}
                className="w-24 h-24 rounded-xl object-cover group-hover:scale-105 transition-transform"
              />
              <div className="flex-1 space-y-1.5 min-w-0">
                <div className="flex items-center space-x-2">
                  <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded ${
                    space.status === 'LIVE' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-700 text-slate-300'
                  }`}>
                    {space.status}
                  </span>
                  <span className="text-xs text-slate-400 flex items-center space-x-1">
                    <Users className="w-3.5 h-3.5 text-indigo-400" />
                    <span>{space.participantsCount} participants</span>
                  </span>
                </div>
                <h3 className="text-base font-bold text-white truncate group-hover:text-brand-purple transition-colors">
                  {space.name}
                </h3>
                <p className="text-xs text-slate-300">
                  Watching: <strong className="text-white">{space.titleName}</strong> &bull; Host: {space.hostName}
                </p>
                <div className="pt-1 flex items-center justify-between text-[11px] text-slate-500">
                  <span>Code: <code className="text-indigo-300 font-mono">{space.inviteCode}</code></span>
                  <span className="text-indigo-400 font-medium group-hover:translate-x-1 transition-transform">
                    Enter Space &rarr;
                  </span>
                </div>
              </div>
            </GlassCard>
          ))}
        </div>
      </section>

      {/* Continue Watching Rail */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Film className="w-5 h-5 text-brand-purple" />
            <h2 className="text-xl font-bold text-white tracking-tight">
              Continue Watching
            </h2>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          {continueWatching.map((item) => (
            <div
              key={item.id}
              className="glass-panel rounded-2xl overflow-hidden group hover:border-indigo-500/40 transition-all cursor-pointer"
              onClick={() => navigate(`/watch/${item.spaceId}`)}
            >
              <div className="aspect-[16/9] relative overflow-hidden">
                <img
                  src={item.posterUrl}
                  alt={item.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-obsidian-950 via-transparent to-transparent" />
                <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/40">
                  <div className="w-12 h-12 rounded-full bg-brand-purple flex items-center justify-center text-white shadow-xl scale-90 group-hover:scale-100 transition-transform">
                    <Play className="w-5 h-5 fill-white ml-0.5" />
                  </div>
                </div>
                {/* Progress Bar */}
                <div className="absolute bottom-0 left-0 right-0 h-1 bg-obsidian-900">
                  <div
                    className="h-full bg-brand-purple"
                    style={{ width: `${item.progress}%` }}
                  />
                </div>
              </div>
              <div className="p-4 space-y-1">
                <h4 className="text-sm font-bold text-white group-hover:text-brand-purple transition-colors truncate">
                  {item.name}
                </h4>
                <div className="flex items-center justify-between text-[11px] text-slate-400">
                  <span>{item.genre}</span>
                  <span>{item.duration}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Join By Code Modal */}
      {showJoinModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-obsidian-950/80 backdrop-blur-md animate-fade-in">
          <GlassCard className="max-w-md w-full p-6 space-y-4 border-indigo-500/30">
            <h3 className="text-lg font-bold text-white tracking-tight flex items-center space-x-2">
              <KeyRound className="w-5 h-5 text-brand-purple" />
              <span>Join Watch Space with Invite Code</span>
            </h3>
            <p className="text-xs text-slate-300 leading-relaxed">
              Enter the unique invite code shared by the party host (e.g. <code className="text-indigo-400">CYBER-2026</code>) to jump into the synchronized playback stream.
            </p>

            <form onSubmit={handleJoinByCode} className="space-y-4">
              <input
                type="text"
                required
                value={inviteCodeInput}
                onChange={(e) => setInviteCodeInput(e.target.value)}
                placeholder="CYBER-2026"
                className="w-full px-4 py-3 rounded-xl bg-obsidian-900/80 border border-slate-700 text-white text-center font-mono tracking-widest text-lg uppercase placeholder-slate-600 focus:outline-none focus:border-brand-purple focus:ring-1 focus:ring-brand-purple transition-all"
              />

              <div className="flex items-center space-x-3 pt-2">
                <CinematicButton
                  type="button"
                  variant="secondary"
                  size="md"
                  onClick={() => setShowJoinModal(false)}
                  className="flex-1"
                >
                  Cancel
                </CinematicButton>
                <CinematicButton
                  type="submit"
                  variant="primary"
                  size="md"
                  disabled={isJoining}
                  className="flex-1"
                >
                  {isJoining ? 'Joining...' : 'Join Room'}
                </CinematicButton>
              </div>
            </form>
          </GlassCard>
        </div>
      )}

      {/* Create Watch Space Modal */}
      <CreateWatchSpaceModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleCreatedSpace}
      />

    </div>
  );
}
