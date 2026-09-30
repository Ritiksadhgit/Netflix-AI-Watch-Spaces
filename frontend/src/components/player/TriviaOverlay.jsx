import React, { useState, useEffect } from 'react';

export default function TriviaOverlay({ trivia, onClose, onAnswered }) {
  const [secondsRemaining, setSecondsRemaining] = useState(trivia?.durationSeconds || 15);
  const [selectedOption, setSelectedOption] = useState(null);
  const [isRevealed, setIsRevealed] = useState(false);

  useEffect(() => {
    if (!trivia) return;
    setSecondsRemaining(trivia.durationSeconds || 15);
    setSelectedOption(null);
    setIsRevealed(false);

    const timer = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          setIsRevealed(true);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [trivia]);

  if (!trivia) return null;

  const handleSelectOption = (idx) => {
    if (isRevealed) return;
    setSelectedOption(idx);
    setIsRevealed(true);
    const correct = idx === trivia.answerIndex;
    if (onAnswered) {
      onAnswered(idx, correct);
    }
  };

  const isCorrect = selectedOption === trivia.answerIndex;

  return (
    <div className="absolute inset-0 z-30 flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-fade-in pointer-events-auto">
      <div className="relative w-full max-w-lg bg-obsidian-900/95 border border-amber-500/30 rounded-3xl p-6 sm:p-7 shadow-2xl shadow-amber-500/10 space-y-4 overflow-hidden">
        {/* Top Accent Gradient */}
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-amber-500 via-orange-500 to-indigo-500" />

        {/* Header with Countdown */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span className="p-1.5 rounded-lg bg-amber-500/20 text-amber-400 font-bold text-xs">
              ⚡ SCENE TRIVIA
            </span>
            <span className="text-[11px] text-gray-400 uppercase tracking-wider font-semibold">
              Live Room Challenge
            </span>
          </div>

          <div className="flex items-center space-x-2">
            <div className={`w-8 h-8 rounded-full border-2 flex items-center justify-center text-xs font-mono font-bold ${
              secondsRemaining <= 3
                ? 'border-red-500 text-red-400 animate-ping'
                : 'border-amber-400 text-amber-300'
            }`}>
              {secondsRemaining}
            </div>
            <button
              onClick={onClose}
              className="p-1 text-gray-400 hover:text-white rounded-lg hover:bg-white/10 transition-colors"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Question Text */}
        <div className="space-y-1">
          <h3 className="text-base sm:text-lg font-bold text-white tracking-tight leading-snug">
            {trivia.question}
          </h3>
          <p className="text-xs text-gray-400">Answer before time runs out!</p>
        </div>

        {/* Options Grid */}
        <div className="grid grid-cols-1 gap-2.5 pt-1">
          {trivia.options.map((opt, idx) => {
            const isSelected = selectedOption === idx;
            const isAnswer = idx === trivia.answerIndex;

            let btnStyle = 'bg-white/5 border-white/10 text-gray-200 hover:bg-white/10 hover:border-white/20';

            if (isRevealed) {
              if (isAnswer) {
                btnStyle = 'bg-emerald-500/20 border-emerald-500 text-emerald-300 ring-2 ring-emerald-500/40 font-semibold';
              } else if (isSelected && !isAnswer) {
                btnStyle = 'bg-red-500/20 border-red-500 text-red-300 ring-2 ring-red-500/40';
              } else {
                btnStyle = 'bg-white/5 border-white/5 text-gray-500 opacity-60';
              }
            }

            return (
              <button
                key={idx}
                type="button"
                disabled={isRevealed}
                onClick={() => handleSelectOption(idx)}
                className={`w-full p-3 rounded-xl border text-left text-xs sm:text-sm transition-all flex items-center justify-between ${btnStyle}`}
              >
                <div className="flex items-center space-x-3">
                  <span className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-xs font-mono font-bold">
                    {String.fromCharCode(65 + idx)}
                  </span>
                  <span>{opt}</span>
                </div>
                {isRevealed && isAnswer && (
                  <span className="text-emerald-400 font-bold text-xs flex items-center space-x-1">
                    <span>✓</span>
                    <span>Correct</span>
                  </span>
                )}
                {isRevealed && isSelected && !isAnswer && (
                  <span className="text-red-400 font-bold text-xs flex items-center space-x-1">
                    <span>✕</span>
                    <span>Incorrect</span>
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Feedback & Production Trivia Note */}
        {isRevealed && (
          <div className="pt-2 space-y-3 animate-fade-in">
            {selectedOption !== null && (
              <div className={`p-3 rounded-xl border flex items-center justify-between ${
                isCorrect
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                  : 'bg-red-500/10 border-red-500/30 text-red-300'
              }`}>
                <div className="flex items-center space-x-2 text-xs font-semibold">
                  <span>{isCorrect ? '🎉 Outstanding!' : '💡 Better luck next scene!'}</span>
                </div>
                {isCorrect && (
                  <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-mono text-xs font-bold">
                    +100 PTS
                  </span>
                )}
              </div>
            )}

            {trivia.triviaNote && (
              <div className="p-3 bg-white/5 rounded-xl border border-white/10 text-xs text-gray-300 leading-relaxed">
                <span className="text-amber-400 font-semibold uppercase tracking-wider text-[10px] block mb-1">
                  Behind The Scenes
                </span>
                {trivia.triviaNote}
              </div>
            )}

            <button
              onClick={onClose}
              className="w-full py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 text-white font-semibold text-xs transition-colors shadow-lg"
            >
              Resume Watching
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
