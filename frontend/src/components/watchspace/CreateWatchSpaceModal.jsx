import React, { useState, useEffect } from 'react';
import { apiClient } from '../../services/apiClient';
import { watchSpaceService } from '../../services/watchSpaceService';
import { useToast } from '../../context/ToastContext';

export default function CreateWatchSpaceModal({ isOpen, onClose, onCreated, preselectedTitleId }) {
  const { addToast } = useToast();
  const [titles, setTitles] = useState([]);
  const [loadingTitles, setLoadingTitles] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form state
  const [selectedTitleId, setSelectedTitleId] = useState('');
  const [name, setName] = useState('');
  const [privacy, setPrivacy] = useState('PUBLIC');
  const [aiVerbosity, setAiVerbosity] = useState('NORMAL');
  const [maxParticipants, setMaxParticipants] = useState(50);
  const [votingEnabled, setVotingEnabled] = useState(true);

  useEffect(() => {
    if (isOpen) {
      setLoadingTitles(true);
      apiClient('/api/v1/titles')
        .then((data) => {
          if (Array.isArray(data) && data.length > 0) {
            setTitles(data);
            const targetTitle = (preselectedTitleId && data.find((d) => d.id.toString() === preselectedTitleId.toString())) || data[0];
            setSelectedTitleId(targetTitle.id.toString());
            setName(`${targetTitle.name} Watch Space`);
          }
        })
        .catch((err) => {
          console.warn('Failed to load titles', err);
        })
        .finally(() => setLoadingTitles(false));
    }
  }, [isOpen, preselectedTitleId]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedTitleId) {
      addToast('Please select a title to watch', 'error');
      return;
    }
    if (!name.trim()) {
      addToast('Please enter a Watch Space name', 'error');
      return;
    }

    try {
      setIsSubmitting(true);
      const payload = {
        titleId: parseInt(selectedTitleId, 10),
        name: name.trim(),
        privacy,
        maxParticipants: parseInt(maxParticipants, 10),
        aiVerbosity,
        votingEnabled,
      };

      const newSpace = await watchSpaceService.createWatchSpace(payload);
      addToast('Watch Space created successfully!', 'success');
      if (onCreated) {
        onCreated(newSpace);
      }
      onClose();
    } catch (err) {
      addToast(err.detail || err.message || 'Failed to create Watch Space', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const selectedTitle = titles.find((t) => t.id.toString() === selectedTitleId);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
      <div className="relative w-full max-w-xl bg-obsidian-900 border border-white/15 rounded-3xl p-6 sm:p-8 shadow-2xl overflow-hidden">
        {/* Ambient Top Glow */}
        <div className="absolute top-0 left-1/4 right-1/4 h-1 bg-gradient-to-r from-indigo-500 via-violet-500 to-indigo-500 blur-sm" />

        <div className="flex items-center justify-between pb-5 border-b border-white/10">
          <div>
            <h2 className="text-2xl font-bold text-white tracking-tight">Create a Watch Space</h2>
            <p className="text-xs text-gray-400 mt-0.5">Stream together with synchronized playback and AI co-pilot</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-white rounded-full hover:bg-white/10 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          {/* Select Title */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-2">
              Select Title
            </label>
            {loadingTitles ? (
              <div className="h-12 bg-white/5 rounded-xl animate-pulse" />
            ) : (
              <div className="grid grid-cols-3 gap-3">
                {titles.map((t) => {
                  const isSelected = selectedTitleId === t.id.toString();
                  return (
                    <button
                      key={t.id}
                      type="button"
                      onClick={() => {
                        setSelectedTitleId(t.id.toString());
                        setName(`${t.name} Watch Space`);
                      }}
                      className={`relative flex flex-col rounded-xl overflow-hidden border text-left transition-all ${
                        isSelected
                          ? 'border-indigo-500 ring-2 ring-indigo-500/50 scale-[1.02]'
                          : 'border-white/10 hover:border-white/30 opacity-70 hover:opacity-100'
                      }`}
                    >
                      <img src={t.posterUrl} alt={t.name} className="w-full h-24 object-cover" />
                      <div className="p-2 bg-obsidian-950/80">
                        <span className="text-xs font-semibold text-white truncate block">{t.name}</span>
                        <span className="text-[10px] text-gray-400">{Math.floor(t.durationSeconds / 60)} mins</span>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Watch Space Name */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-1.5">
              Watch Space Name
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Cyberpunk Sci-Fi Night"
              className="w-full px-4 py-3 bg-obsidian-950 border border-white/15 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm"
            />
          </div>

          {/* Privacy & Max Participants Row */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-1.5">
                Privacy
              </label>
              <select
                value={privacy}
                onChange={(e) => setPrivacy(e.target.value)}
                className="w-full px-4 py-3 bg-obsidian-950 border border-white/15 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
              >
                <option value="PUBLIC">Public (Listed in Dashboard)</option>
                <option value="INVITE_ONLY">Invite Only (Code Required)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-1.5">
                Max Participants ({maxParticipants})
              </label>
              <input
                type="range"
                min="2"
                max="100"
                value={maxParticipants}
                onChange={(e) => setMaxParticipants(e.target.value)}
                className="w-full h-2 accent-indigo-500 bg-white/10 rounded-lg cursor-pointer mt-4"
              />
            </div>
          </div>

          {/* AI Verbosity & Narrative Voting */}
          <div className="grid grid-cols-2 gap-4 pt-1">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-1.5">
                AI Co-Pilot Verbosity
              </label>
              <select
                value={aiVerbosity}
                onChange={(e) => setAiVerbosity(e.target.value)}
                className="w-full px-4 py-3 bg-obsidian-950 border border-white/15 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
              >
                <option value="MINIMAL">Minimal (Only key moments)</option>
                <option value="NORMAL">Normal (Balanced trivia & Q&A)</option>
                <option value="CHATTY">Chatty (Deep-dive insights)</option>
              </select>
            </div>

            <div className="flex flex-col justify-end">
              <label className="flex items-center space-x-3 p-3 bg-obsidian-950 border border-white/10 rounded-xl cursor-pointer hover:border-white/20 transition-colors">
                <input
                  type="checkbox"
                  checked={votingEnabled}
                  onChange={(e) => setVotingEnabled(e.target.checked)}
                  className="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 bg-obsidian-900 border-white/30"
                />
                <span className="text-xs font-medium text-gray-200">Enable Narrative Branch Voting</span>
              </label>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 rounded-xl text-sm font-semibold text-gray-400 hover:text-white hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-6 py-2.5 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 shadow-lg shadow-indigo-500/25 transition-all flex items-center space-x-2 disabled:opacity-50"
            >
              {isSubmitting ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Launching...</span>
                </>
              ) : (
                <span>Launch Watch Space</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
