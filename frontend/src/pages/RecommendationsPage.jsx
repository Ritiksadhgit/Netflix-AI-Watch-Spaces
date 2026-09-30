import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { recommendationService } from '../services/recommendationService';
import GlassCard from '../components/common/GlassCard';
import CinematicButton from '../components/common/CinematicButton';
import CreateWatchSpaceModal from '../components/watchspace/CreateWatchSpaceModal';
import { 
  Sparkles, 
  Play, 
  PlusCircle, 
  Flame, 
  Film, 
  Compass, 
  Star 
} from 'lucide-react';

export default function RecommendationsPage() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState({});
  const [loading, setLoading] = useState(true);
  const [selectedTitle, setSelectedTitle] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [preselectedTitleId, setPreselectedTitleId] = useState(null);

  useEffect(() => {
    setLoading(true);
    recommendationService.getCategorizedRecommendations()
      .then((data) => {
        if (data && typeof data === 'object' && Object.keys(data).length > 0) {
          setCategories(data);
        } else {
          // Sample fallback
          setCategories({
            'Top Picks for You': [
              {
                id: 1,
                name: 'Tears of Steel',
                synopsis: 'Set in a dystopian future Amsterdam, a desperate group of warriors battle rogue cybernetic drones.',
                durationSeconds: 734,
                posterUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80',
                genres: 'Sci-Fi, Cyberpunk',
                releaseYear: 2024,
                matchScore: 98,
                matchReason: 'Top Trending in Sci-Fi',
              },
              {
                id: 2,
                name: 'Sintel',
                synopsis: 'A lonely young tracker named Sintel rescues and nurses a wounded baby dragon, forming an unbreakable bond.',
                durationSeconds: 888,
                posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
                genres: 'Fantasy, Adventure',
                releaseYear: 2023,
                matchScore: 94,
                matchReason: 'Critically Acclaimed Community Choice',
              },
            ],
            'Animation & Fantasy Quests': [
              {
                id: 3,
                name: 'Big Buck Bunny',
                synopsis: 'A colossal, benevolent forest rabbit stands up to a gang of mischievous bullies.',
                durationSeconds: 596,
                posterUrl: 'https://images.unsplash.com/photo-1535083783855-76ae62b2914e?auto=format&fit=crop&w=600&q=80',
                genres: 'Animation, Comedy',
                releaseYear: 2022,
                matchScore: 89,
                matchReason: 'Family Favorite & Lighthearted Fun',
              },
            ]
          });
        }
      })
      .catch((err) => {
        console.warn('Failed to load categorized recommendations', err);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleStartParty = (titleId) => {
    setPreselectedTitleId(titleId);
    setShowCreateModal(true);
  };

  const handleCreatedSpace = (newSpace) => {
    navigate(`/watch/${newSpace.id}`);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-12">
      {/* Header Banner */}
      <div className="relative rounded-3xl p-8 sm:p-10 glass-panel border-white/10 overflow-hidden glow-indigo">
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <div className="w-14 h-14 rounded-2xl bg-brand-purple/20 border border-brand-purple/30 flex items-center justify-center text-brand-purple shadow-xl">
              <Sparkles className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  AI-Powered Recommendations
                </h1>
                <span className="text-xs px-2.5 py-0.5 rounded-full bg-brand-purple/20 text-brand-purple border border-brand-purple/30 font-semibold">
                  Personalized
                </span>
              </div>
              <p className="text-xs sm:text-sm text-slate-300 mt-1">
                Content-grounded scoring calibrated from your viewing habits and community trends
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Categorized Recommendation Rails */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center space-y-3">
          <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-xs text-slate-400">Curating personalized recommendation rails...</span>
        </div>
      ) : (
        <div className="space-y-12">
          {Object.entries(categories).map(([categoryTitle, titles]) => (
            <section key={categoryTitle} className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-bold text-white tracking-tight flex items-center space-x-2">
                  <Flame className="w-5 h-5 text-amber-400" />
                  <span>{categoryTitle}</span>
                </h2>
                <span className="text-xs text-slate-400">
                  {titles.length} {titles.length === 1 ? 'title' : 'titles'} curated
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {titles.map((title) => (
                  <GlassCard
                    key={`${categoryTitle}-${title.id}`}
                    hoverEffect
                    className="p-5 flex flex-col justify-between space-y-4 border-white/10 group"
                  >
                    <div className="space-y-3">
                      {/* Thumbnail with Match Score badge */}
                      <div className="aspect-video relative rounded-xl overflow-hidden">
                        <img
                          src={title.posterUrl}
                          alt={title.name}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-obsidian-950 via-transparent to-transparent" />

                        {/* Match Score Badge */}
                        <div className="absolute top-3 right-3 px-2.5 py-1 rounded-full bg-emerald-500/90 backdrop-blur-md text-obsidian-950 text-xs font-black shadow-lg">
                          {title.matchScore}% Match
                        </div>
                      </div>

                      {/* Title Info */}
                      <div>
                        <h3 className="text-base font-bold text-white group-hover:text-brand-purple transition-colors truncate">
                          {title.name}
                        </h3>
                        <p className="text-xs text-indigo-300 font-medium mt-0.5">
                          {title.matchReason || 'Recommended for You'}
                        </p>
                        <p className="text-xs text-slate-400 line-clamp-2 mt-2 leading-relaxed">
                          {title.synopsis}
                        </p>
                      </div>
                    </div>

                    {/* Metadata & Actions */}
                    <div className="pt-2 border-t border-white/10 flex items-center justify-between">
                      <span className="text-[11px] text-slate-400">
                        {Math.floor((title.durationSeconds || 0) / 60)} mins &bull; {title.genres}
                      </span>

                      <CinematicButton
                        variant="primary"
                        size="sm"
                        onClick={() => handleStartParty(title.id)}
                        className="text-xs"
                      >
                        <PlusCircle className="w-3.5 h-3.5 mr-1" />
                        <span>Host Space</span>
                      </CinematicButton>
                    </div>
                  </GlassCard>
                ))}
              </div>
            </section>
          ))}
        </div>
      )}

      {/* Create Watch Space Modal */}
      <CreateWatchSpaceModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleCreatedSpace}
        preselectedTitleId={preselectedTitleId}
      />
    </div>
  );
}
