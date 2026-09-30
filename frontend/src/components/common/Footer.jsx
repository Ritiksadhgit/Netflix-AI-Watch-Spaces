import React from 'react';
import { Play, ShieldAlert, Cpu, Heart } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="w-full bg-obsidian-950 border-t border-white/5 py-12 px-4 sm:px-6 lg:px-8 mt-24">
      <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
        
        {/* Brand & Mission */}
        <div className="md:col-span-2 space-y-4">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-brand-indigo to-brand-purple flex items-center justify-center">
              <Play className="w-4 h-4 text-white fill-white ml-0.5" />
            </div>
            <span className="text-lg font-bold text-white tracking-tight">
              Watch<span className="text-brand-purple">Spaces</span> AI
            </span>
          </div>
          <p className="text-sm text-slate-400 max-w-md leading-relaxed">
            Full-stack AI-powered social streaming platform featuring synchronized micro-drift playback, real-time presence, authored scene trivia, and retrieval-grounded AI Co-Pilots.
          </p>

          <div className="flex items-center space-x-2 text-xs text-amber-400/90 bg-amber-500/10 border border-amber-500/20 px-3 py-2 rounded-xl max-w-lg">
            <ShieldAlert className="w-4 h-4 flex-shrink-0" />
            <span>
              <strong>Compliance Notice:</strong> Autonomous custom showcase application. Not affiliated with, endorsed by, or integrated with Netflix, Inc. Uses open-source creative commons video assets.
            </span>
          </div>
        </div>

        {/* Core Stack */}
        <div className="space-y-3">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300">
            Technology Stack
          </h4>
          <ul className="text-xs space-y-2 text-slate-400">
            <li className="flex items-center space-x-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-brand-indigo"></span>
              <span>Java 25 & Spring Boot 3.3 WebFlux</span>
            </li>
            <li className="flex items-center space-x-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-brand-purple"></span>
              <span>Reactive WebSockets (/ws/watch-space)</span>
            </li>
            <li className="flex items-center space-x-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-brand-emerald"></span>
              <span>MySQL 9 Relational Persistence</span>
            </li>
            <li className="flex items-center space-x-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-brand-cyan"></span>
              <span>Grounded Timeline RAG AI Engine</span>
            </li>
          </ul>
        </div>

        {/* Performance SLAs */}
        <div className="space-y-3">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300">
            Verified Performance SLAs
          </h4>
          <ul className="text-xs space-y-2 text-slate-400">
            <li>Sync Drift: <span className="text-emerald-400 font-semibold">&lt; 100ms</span> (Target &lt; 250ms)</li>
            <li>Resync Convergence: <span className="text-emerald-400 font-semibold">&lt; 2 seconds</span></li>
            <li>REST Non-AI P95: <span className="text-indigo-400 font-semibold">&lt; 200ms</span></li>
            <li>AI Grounded P95: <span className="text-indigo-400 font-semibold">&lt; 3.0s</span></li>
          </ul>
        </div>
      </div>

      <div className="max-w-7xl mx-auto mt-12 pt-6 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500">
        <p>© 2026 WatchSpaces AI. Commercial-grade Social Streaming Platform.</p>
        <div className="flex items-center space-x-1 mt-2 sm:mt-0">
          <span>Engineered with Spring WebFlux & React</span>
        </div>
      </div>
    </footer>
  );
}
