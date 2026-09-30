import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import CinematicButton from '../components/common/CinematicButton';
import GlassCard from '../components/common/GlassCard';
import { 
  Play, 
  Sparkles, 
  Users, 
  Radio, 
  Layers, 
  Vote, 
  ArrowRight, 
  CheckCircle,
  Clock,
  Tv
} from 'lucide-react';

export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleCta = () => {
    if (isAuthenticated) navigate('/dashboard');
    else navigate('/register');
  };

  const features = [
    {
      icon: Radio,
      title: 'Sub-100ms Synchronized Playback',
      description: 'Host-authoritative clock arbiter with gentle micro-drift rate modulation keeps all participants strictly in sync without audio stutter.',
      accent: 'indigo',
    },
    {
      icon: Sparkles,
      title: 'Retrieval-Grounded AI Co-Pilot',
      description: 'Ask questions about who is on screen or what tech is being deployed. The AI retrieves authored scene events and cites timeline source IDs.',
      accent: 'purple',
    },
    {
      icon: Layers,
      title: 'Authored Scene Trivia',
      description: 'Trivia challenges pop up synchronously across all clients at designated timestamps with automated answer evaluation.',
      accent: 'emerald',
    },
    {
      icon: Vote,
      title: 'Narrative Branch Voting',
      description: 'At critical storyline variation forks, participant votes decide tactical decisions and dynamically switch chapter playback in unison.',
      accent: 'amber',
    },
  ];

  return (
    <div className="relative overflow-hidden">
      
      {/* Hero Section */}
      <section className="relative pt-20 pb-28 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto text-center">
        
        {/* Ambient Top Glow */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-brand-indigo/15 rounded-full blur-[140px] pointer-events-none" />

        {/* Live Pill Badge */}
        <div className="inline-flex items-center space-x-2 px-4 py-1.5 rounded-full glass-panel border-indigo-500/30 text-xs font-semibold text-indigo-300 mb-8 animate-pulse-subtle">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
          <span>Real-Time Social Watch Platform &bull; Java WebFlux + React</span>
        </div>

        {/* Main Headline */}
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-white max-w-5xl mx-auto leading-[1.1]">
          Experience Cinema Together,{' '}
          <span className="bg-gradient-to-r from-brand-indigo via-brand-purple to-cyan-400 bg-clip-text text-transparent">
            Elevated by Real-Time AI.
          </span>
        </h1>

        {/* Subtitle */}
        <p className="mt-6 text-lg sm:text-xl text-slate-300 max-w-2xl mx-auto font-normal leading-relaxed">
          Create synchronized Watch Spaces with authoritative playback control, interactive scene trivia, real-time chat, and a timeline-grounded AI Co-Pilot.
        </p>

        {/* Call to Actions */}
        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-5">
          <CinematicButton
            size="lg"
            onClick={handleCta}
            className="w-full sm:w-auto text-base px-8 py-4 glow-indigo"
          >
            <span>{isAuthenticated ? 'Enter Dashboard' : 'Start Watching Together'}</span>
            <ArrowRight className="w-5 h-5 ml-2" />
          </CinematicButton>

          <Link to="/login" className="w-full sm:w-auto">
            <CinematicButton
              variant="glass"
              size="lg"
              className="w-full sm:w-auto text-base px-8 py-4"
            >
              <Play className="w-4 h-4 mr-2 text-brand-purple fill-brand-purple" />
              <span>Explore Demo Space</span>
            </CinematicButton>
          </Link>
        </div>

        {/* Hero Video Preview Mockup */}
        <div className="mt-16 max-w-5xl mx-auto rounded-3xl p-2.5 glass-panel border-white/10 glow-purple">
          <div className="relative rounded-2xl overflow-hidden aspect-video bg-obsidian-900 border border-white/10 group">
            <img
              src="https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1920&q=80"
              alt="Cinematic Preview"
              className="w-full h-full object-cover opacity-85 group-hover:scale-102 transition-transform duration-700"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-obsidian-950 via-obsidian-950/20 to-transparent" />

            {/* Overlaid UI Badges */}
            <div className="absolute top-6 left-6 flex items-center space-x-3">
              <div className="px-3 py-1.5 rounded-lg bg-emerald-500/20 border border-emerald-500/40 text-emerald-300 text-xs font-bold uppercase tracking-wider flex items-center space-x-1.5">
                <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                <span>In-Sync (32ms drift)</span>
              </div>
              <div className="px-3 py-1.5 rounded-lg bg-obsidian-950/70 backdrop-blur-md border border-white/10 text-white text-xs font-medium flex items-center space-x-1.5">
                <Users className="w-3.5 h-3.5 text-indigo-400" />
                <span>8 Watching Now</span>
              </div>
            </div>

            {/* Floating AI Grounded Context Chip */}
            <div className="absolute bottom-6 right-6 max-w-sm glass-panel p-4 rounded-xl text-left border-indigo-500/30">
              <div className="flex items-center space-x-2 text-xs font-semibold text-brand-purple mb-1">
                <Sparkles className="w-3.5 h-3.5" />
                <span>AI Co-Pilot &bull; Scene Context</span>
              </div>
              <p className="text-xs text-slate-200 leading-snug">
                "Thom retrofits heavy cybernetic actuators at Amsterdam Bridge. Authored Event #1 cited."
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Grid Section */}
      <section className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16 space-y-4">
          <h2 className="text-xs font-bold uppercase tracking-widest text-brand-purple">
            Platform Capabilities
          </h2>
          <h3 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Engineered for Precision, Real-Time Presence & Grounded Intelligence
          </h3>
          <p className="text-sm sm:text-base text-slate-400">
            A real-time reactive architecture built with Spring WebFlux, Project Reactor, and pure HTML5 video synchronization.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, idx) => {
            const Icon = feature.icon;
            return (
              <GlassCard
                key={idx}
                hoverEffect
                className="flex flex-col justify-between space-y-4 border-white/5"
              >
                <div className="w-12 h-12 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center text-brand-purple">
                  <Icon className="w-6 h-6" />
                </div>
                <div className="space-y-2">
                  <h4 className="text-lg font-bold text-white tracking-tight">
                    {feature.title}
                  </h4>
                  <p className="text-xs sm:text-sm text-slate-400 leading-relaxed">
                    {feature.description}
                  </p>
                </div>
              </GlassCard>
            );
          })}
        </div>
      </section>

      {/* Featured Catalog Titles Section */}
      <section className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto border-t border-white/5">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h3 className="text-2xl font-bold text-white tracking-tight">
              Featured Open Movies Catalog
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Curated legal open assets ready for immediate synchronized streaming
            </p>
          </div>
          <Link
            to="/login"
            className="text-xs font-semibold text-brand-purple hover:text-indigo-300 flex items-center space-x-1"
          >
            <span>Explore All Titles</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          
          {/* Title 1 */}
          <div className="glass-panel rounded-2xl overflow-hidden group hover:border-indigo-500/40 transition-all duration-300">
            <div className="aspect-[16/9] relative overflow-hidden">
              <img
                src="https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80"
                alt="Tears of Steel"
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-obsidian-950 via-transparent to-transparent" />
              <span className="absolute top-3 left-3 text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                Sci-Fi VFX
              </span>
            </div>
            <div className="p-5 space-y-2">
              <h4 className="text-base font-bold text-white group-hover:text-brand-purple transition-colors">
                Tears of Steel
              </h4>
              <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                Resistance fighters combat rogue cyborg drones in a dystopian Amsterdam with authored timeline trivia.
              </p>
              <div className="flex items-center justify-between pt-2 text-[11px] text-slate-500">
                <span className="flex items-center space-x-1">
                  <Clock className="w-3 h-3" />
                  <span>12 min &bull; 4K</span>
                </span>
                <span className="text-emerald-400 font-medium">Timeline Authored</span>
              </div>
            </div>
          </div>

          {/* Title 2 */}
          <div className="glass-panel rounded-2xl overflow-hidden group hover:border-indigo-500/40 transition-all duration-300">
            <div className="aspect-[16/9] relative overflow-hidden">
              <img
                src="https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80"
                alt="Sintel"
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-obsidian-950 via-transparent to-transparent" />
              <span className="absolute top-3 left-3 text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-brand-purple/20 text-brand-purple border border-brand-purple/30">
                Fantasy
              </span>
            </div>
            <div className="p-5 space-y-2">
              <h4 className="text-base font-bold text-white group-hover:text-brand-purple transition-colors">
                Sintel
              </h4>
              <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                An epic quest across treacherous snowy wastes in search of a stolen dragon companion.
              </p>
              <div className="flex items-center justify-between pt-2 text-[11px] text-slate-500">
                <span className="flex items-center space-x-1">
                  <Clock className="w-3 h-3" />
                  <span>15 min &bull; HD</span>
                </span>
                <span className="text-emerald-400 font-medium">Timeline Authored</span>
              </div>
            </div>
          </div>

          {/* Title 3 */}
          <div className="glass-panel rounded-2xl overflow-hidden group hover:border-indigo-500/40 transition-all duration-300">
            <div className="aspect-[16/9] relative overflow-hidden">
              <img
                src="https://images.unsplash.com/photo-1535083783855-76ae62b2914e?auto=format&fit=crop&w=600&q=80"
                alt="Big Buck Bunny"
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-obsidian-950 via-transparent to-transparent" />
              <span className="absolute top-3 left-3 text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/30">
                Animation
              </span>
            </div>
            <div className="p-5 space-y-2">
              <h4 className="text-base font-bold text-white group-hover:text-brand-purple transition-colors">
                Big Buck Bunny
              </h4>
              <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                A peaceful giant rabbit defends his forest friends from forest bullies in this beloved animation.
              </p>
              <div className="flex items-center justify-between pt-2 text-[11px] text-slate-500">
                <span className="flex items-center space-x-1">
                  <Clock className="w-3 h-3" />
                  <span>10 min &bull; 4K</span>
                </span>
                <span className="text-emerald-400 font-medium">Timeline Authored</span>
              </div>
            </div>
          </div>

        </div>
      </section>

      {/* Ready to Stream CTA Banner */}
      <section className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="relative rounded-3xl p-8 sm:p-12 overflow-hidden bg-gradient-to-r from-brand-indigo/20 via-brand-purple/20 to-transparent border border-indigo-500/30 text-center space-y-6">
          <h3 className="text-3xl sm:text-4xl font-bold text-white tracking-tight max-w-2xl mx-auto">
            Ready to experience the next evolution of synchronized streaming?
          </h3>
          <p className="text-sm sm:text-base text-slate-300 max-w-xl mx-auto">
            Create a Watch Space in seconds, share the invite code with your friends, and watch with real-time AI context.
          </p>
          <CinematicButton
            size="lg"
            onClick={handleCta}
            className="px-8 py-3.5 glow-indigo"
          >
            <span>{isAuthenticated ? 'Go to Dashboard' : 'Create Free Account'}</span>
            <ArrowRight className="w-5 h-5 ml-2" />
          </CinematicButton>
        </div>
      </section>

    </div>
  );
}
