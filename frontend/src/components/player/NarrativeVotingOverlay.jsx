import React, { useState, useEffect } from 'react';

export default function NarrativeVotingOverlay({
  variation,
  result,
  isHost = false,
  onVote,
  onFinalize,
  onClose,
}) {
  const [selectedOptionId, setSelectedOptionId] = useState(null);
  const [timeLeft, setTimeLeft] = useState(20);

  // Initialize and run countdown timer
  useEffect(() => {
    if (!variation) return;

    const expiresAt = variation.expiresAtMs || (Date.now() + (variation.durationSeconds || 20) * 1000);
    const initialRemaining = Math.max(0, Math.round((expiresAt - Date.now()) / 1000));
    setTimeLeft(initialRemaining);

    const interval = setInterval(() => {
      const remaining = Math.max(0, Math.round((expiresAt - Date.now()) / 1000));
      setTimeLeft(remaining);
      if (remaining <= 0) {
        clearInterval(interval);
      }
    }, 500);

    return () => clearInterval(interval);
  }, [variation]);

  if (!variation && !result) return null;

  // If a winning branch has been applied, display celebration announcement card
  if (result) {
    return (
      <div className="absolute inset-0 bg-obsidian-950/80 backdrop-blur-md z-40 flex items-center justify-center p-4 animate-fadeIn">
        <div className="bg-obsidian-900/95 border border-indigo-500/50 rounded-2xl p-6 sm:p-8 max-w-lg w-full text-center space-y-5 shadow-2xl relative overflow-hidden">
          {/* Animated decorative glow */}
          <div className="absolute -top-12 -left-12 w-40 h-40 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-12 -right-12 w-40 h-40 bg-violet-500/20 rounded-full blur-3xl pointer-events-none" />

          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-indigo-500/20 border border-indigo-500/40 text-indigo-400 text-3xl shadow-inner mx-auto">
            🎬
          </div>

          <div className="space-y-2">
            <span className="text-xs uppercase font-extrabold tracking-widest text-indigo-400 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20">
              Consensus Reached
            </span>
            <h3 className="text-2xl font-bold text-white tracking-tight pt-1">
              Narrative Branch Applied
            </h3>
            <p className="text-sm text-gray-300">
              The watch party has decided the timeline progression:
            </p>
          </div>

          <div className="p-4 rounded-xl bg-gradient-to-r from-indigo-950/60 to-violet-950/60 border border-indigo-500/40 text-center">
            <p className="text-lg font-bold text-indigo-200">
              "{result.winningOptionLabel}"
            </p>
            <p className="text-xs text-gray-400 mt-1">
              Received {result.totalVotes} watch space {result.totalVotes === 1 ? 'vote' : 'votes'}
            </p>
          </div>

          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm transition-colors shadow-lg"
          >
            Continue Stream
          </button>
        </div>
      </div>
    );
  }

  const options = variation.options || [];
  const totalVotes = variation.totalVotes || options.reduce((sum, o) => sum + (o.votes || o.voteCount || 0), 0);
  const duration = variation.durationSeconds || 20;
  const progressPercent = Math.max(0, Math.min(100, (timeLeft / duration) * 100));

  const handleCastVote = (optId) => {
    setSelectedOptionId(optId);
    if (onVote) {
      onVote(variation.variationEventId, optId);
    }
  };

  return (
    <div className="absolute inset-0 bg-obsidian-950/80 backdrop-blur-md z-40 flex items-center justify-center p-4 animate-fadeIn">
      <div className="bg-obsidian-900/95 border border-white/20 rounded-2xl p-5 sm:p-7 max-w-lg w-full space-y-5 shadow-2xl relative overflow-hidden">
        {/* Top Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span className="w-2.5 h-2.5 rounded-full bg-amber-400 animate-ping" />
            <span className="text-xs uppercase font-extrabold tracking-widest text-amber-400">
              Live Decision Point
            </span>
          </div>

          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white p-1 rounded-lg hover:bg-white/5 transition-colors"
            title="Minimize voting overlay"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Prompt */}
        <div className="space-y-1">
          <h3 className="text-lg sm:text-xl font-bold text-white tracking-tight leading-snug">
            {variation.prompt || 'Choose the next narrative action:'}
          </h3>
          <p className="text-xs text-gray-400">
            Votes are counted in real-time across all room participants.
          </p>
        </div>

        {/* Countdown Progress Bar */}
        <div className="space-y-1.5">
          <div className="flex justify-between items-center text-xs font-mono">
            <span className="text-gray-400">Time remaining:</span>
            <span className={`font-bold ${timeLeft <= 5 ? 'text-red-400 animate-pulse' : 'text-amber-300'}`}>
              {timeLeft}s
            </span>
          </div>
          <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden">
            <div
              className={`h-full transition-all duration-500 rounded-full ${
                timeLeft <= 5
                  ? 'bg-gradient-to-r from-red-500 to-amber-500'
                  : 'bg-gradient-to-r from-amber-400 to-indigo-500'
              }`}
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>

        {/* Voting Options */}
        <div className="space-y-2.5">
          {options.map((opt) => {
            const votes = opt.votes || opt.voteCount || 0;
            const pct = totalVotes > 0 ? Math.round((votes / totalVotes) * 100) : 0;
            const isSelected = selectedOptionId === opt.id;

            return (
              <button
                key={opt.id}
                onClick={() => handleCastVote(opt.id)}
                className={`relative w-full p-3.5 rounded-xl border text-left transition-all overflow-hidden ${
                  isSelected
                    ? 'border-indigo-400 bg-indigo-950/40 ring-1 ring-indigo-400 shadow-md'
                    : 'border-white/10 bg-white/5 hover:bg-white/10 hover:border-white/20'
                }`}
              >
                {/* Background vote proportion bar */}
                <div
                  className="absolute inset-y-0 left-0 bg-indigo-500/15 pointer-events-none transition-all duration-300"
                  style={{ width: `${pct}%` }}
                />

                <div className="relative flex items-center justify-between z-10">
                  <div className="space-y-0.5 pr-2">
                    <span className="text-sm font-semibold text-white block">
                      {opt.label}
                    </span>
                    <span className="text-[11px] text-gray-400 font-mono">
                      {votes} {votes === 1 ? 'vote' : 'votes'} ({pct}%)
                    </span>
                  </div>

                  <div className="shrink-0 flex items-center space-x-2">
                    {isSelected && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/30 text-indigo-300 font-semibold border border-indigo-400/40">
                        Your Vote
                      </span>
                    )}
                    <span className="text-xs text-gray-300 font-bold px-2 py-1 rounded-md bg-white/10">
                      {pct}%
                    </span>
                  </div>
                </div>
              </button>
            );
          })}
        </div>

        {/* Footer with Host actions & Info */}
        <div className="pt-2 flex items-center justify-between border-t border-white/10 text-xs text-gray-400">
          <span>{totalVotes} total {totalVotes === 1 ? 'vote' : 'votes'} cast</span>

          {isHost && (
            <button
              onClick={() => onFinalize && onFinalize(variation.variationEventId)}
              className="px-3 py-1.5 rounded-lg bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 font-semibold border border-amber-500/40 transition-colors"
              title="Lock voting and immediately apply current consensus"
            >
              Lock & Apply Decision
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
