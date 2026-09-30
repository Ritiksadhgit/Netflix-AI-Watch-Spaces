import React, { useState, useEffect } from 'react';
import { apiClient } from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import GlassCard from '../components/common/GlassCard';
import CinematicButton from '../components/common/CinematicButton';
import { Sparkles, Film, Clock, HelpCircle, BookOpen, Layers, Send } from 'lucide-react';

export default function AiCoPilotPage() {
  const { addToast } = useToast();

  const [titles, setTitles] = useState([]);
  const [selectedTitleId, setSelectedTitleId] = useState(1);
  const [selectedTitle, setSelectedTitle] = useState(null);

  const [timelineEvents, setTimelineEvents] = useState([]);
  const [loadingEvents, setLoadingEvents] = useState(true);
  const [selectedEventType, setSelectedEventType] = useState('ALL');
  const [scrubberTime, setScrubberTime] = useState(45);

  const [questionInput, setQuestionInput] = useState('');
  const [isAsking, setIsAsking] = useState(false);
  const [conversation, setConversation] = useState([]);

  // Load Titles
  useEffect(() => {
    apiClient('/api/v1/titles')
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          setTitles(data);
          setSelectedTitleId(data[0].id);
          setSelectedTitle(data[0]);
        }
      })
      .catch((err) => console.warn('Failed to load titles', err));
  }, []);

  // Load Timeline Events when title changes
  useEffect(() => {
    if (selectedTitleId) {
      setLoadingEvents(true);
      apiClient(`/api/v1/titles/${selectedTitleId}/timeline`)
        .then((events) => {
          setTimelineEvents(events);
        })
        .catch((err) => {
          console.warn('Failed to load timeline events', err);
        })
        .finally(() => setLoadingEvents(false));

      const titleObj = titles.find((t) => t.id === selectedTitleId);
      if (titleObj) setSelectedTitle(titleObj);
    }
  }, [selectedTitleId, titles]);

  const handleAskQuestion = async (text) => {
    const q = text || questionInput;
    if (!q.trim()) return;

    const userMessage = {
      role: 'user',
      text: q.trim(),
      timestamp: scrubberTime,
    };

    setConversation((prev) => [...prev, userMessage]);
    setQuestionInput('');
    setIsAsking(true);

    try {
      const response = await apiClient(`/api/v1/titles/${selectedTitleId}/ai/ask`, {
        method: 'POST',
        body: {
          question: q.trim(),
          currentTimestamp: scrubberTime,
        },
      });

      const aiMessage = {
        role: 'assistant',
        text: response.answer,
        currentScene: response.currentScene,
        sources: response.sources || [],
        timestamp: scrubberTime,
      };

      setConversation((prev) => [...prev, aiMessage]);
    } catch (err) {
      addToast(err.message || 'Failed to query AI Co-Pilot', 'error');
    } finally {
      setIsAsking(false);
    }
  };

  const filteredEvents = timelineEvents.filter((ev) => {
    if (selectedEventType === 'ALL') return true;
    return ev.eventType === selectedEventType;
  });

  const formatSeconds = (sec) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const getEventTypeColor = (type) => {
    switch (type) {
      case 'SCENE': return 'bg-blue-500/20 text-blue-300 border-blue-500/30';
      case 'CHARACTER': return 'bg-purple-500/20 text-purple-300 border-purple-500/30';
      case 'TRIVIA': return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
      case 'GLOSSARY': return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30';
      case 'VARIATION': return 'bg-rose-500/20 text-rose-300 border-rose-500/30';
      default: return 'bg-white/10 text-gray-300 border-white/20';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-10">
      {/* Header */}
      <div className="space-y-2">
        <div className="flex items-center space-x-2 text-indigo-400">
          <Sparkles className="w-5 h-5 animate-pulse" />
          <span className="text-xs font-bold uppercase tracking-wider">Retrieval-Grounded Intelligence</span>
        </div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">AI Co-Pilot Studio</h1>
        <p className="text-sm text-gray-400 max-w-2xl">
          Explore authored timeline metadata and query the retrieval-grounded AI engine with verified source citations across catalog titles.
        </p>
      </div>

      {/* Title & Scrubber Controller */}
      <GlassCard className="p-6 space-y-6 border-white/10">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-white/10">
          {/* Select Title */}
          <div className="flex items-center space-x-3">
            <Film className="w-5 h-5 text-indigo-400" />
            <div>
              <label className="text-xs font-semibold uppercase tracking-wider text-gray-400 block mb-1">
                Active Catalog Title
              </label>
              <select
                value={selectedTitleId}
                onChange={(e) => setSelectedTitleId(parseInt(e.target.value, 10))}
                className="bg-obsidian-950 border border-white/20 rounded-xl px-4 py-2 text-sm font-semibold text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                {titles.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name} ({Math.floor(t.durationSeconds / 60)} mins)
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Current Scrubber Timestamp */}
          <div className="flex items-center space-x-4">
            <div className="text-right">
              <span className="text-xs text-gray-400 block">Context Window Timestamp</span>
              <span className="text-xl font-mono font-bold text-indigo-300">
                {formatSeconds(scrubberTime)} ({scrubberTime}s)
              </span>
            </div>
          </div>
        </div>

        {/* Interactive Timeline Scrubber Slider */}
        <div className="space-y-2">
          <div className="flex justify-between text-xs text-gray-400 font-mono">
            <span>00:00</span>
            <span>Window: [{Math.max(0, scrubberTime - 90)}s - {scrubberTime + 30}s]</span>
            <span>{selectedTitle ? formatSeconds(selectedTitle.durationSeconds) : '12:00'}</span>
          </div>
          <input
            type="range"
            min="0"
            max={selectedTitle ? selectedTitle.durationSeconds : 734}
            value={scrubberTime}
            onChange={(e) => setScrubberTime(parseInt(e.target.value, 10))}
            className="w-full h-2.5 bg-white/10 rounded-lg accent-indigo-500 cursor-pointer"
          />
        </div>
      </GlassCard>

      {/* Main Dual Grid: Timeline Events (Left) + AI Playground (Right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        {/* Left Col: Timeline Events Explorer */}
        <div className="lg:col-span-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Layers className="w-5 h-5 text-indigo-400" />
              <h2 className="text-lg font-bold text-white tracking-tight">Authored Timeline Events</h2>
            </div>
            <span className="text-xs text-gray-400 font-mono">
              {filteredEvents.length} events
            </span>
          </div>

          {/* Filter Pills */}
          <div className="flex flex-wrap gap-2">
            {['ALL', 'SCENE', 'CHARACTER', 'TRIVIA', 'GLOSSARY', 'VARIATION'].map((type) => (
              <button
                key={type}
                onClick={() => setSelectedEventType(type)}
                className={`px-3 py-1 rounded-full text-xs font-semibold tracking-wider transition-all ${
                  selectedEventType === type
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                    : 'bg-white/5 text-gray-400 hover:text-white hover:bg-white/10'
                }`}
              >
                {type}
              </button>
            ))}
          </div>

          {/* Event Cards Scroll List */}
          <div className="space-y-3 max-h-[550px] overflow-y-auto pr-1">
            {loadingEvents ? (
              <div className="p-8 text-center text-gray-500 text-sm">Loading authored timeline events...</div>
            ) : filteredEvents.length === 0 ? (
              <div className="p-8 text-center text-gray-500 text-sm">No timeline events found for this filter.</div>
            ) : (
              filteredEvents.map((ev) => {
                let parsed = {};
                try { parsed = JSON.parse(ev.payloadJson); } catch (e) {}

                return (
                  <div
                    key={ev.id}
                    onClick={() => setScrubberTime(ev.tsSeconds)}
                    className="p-4 rounded-2xl bg-obsidian-900/60 border border-white/10 hover:border-indigo-500/40 transition-all cursor-pointer space-y-2 group"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-2">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getEventTypeColor(ev.eventType)}`}>
                          {ev.eventType}
                        </span>
                        <span className="text-xs font-mono font-semibold text-indigo-300">
                          {formatSeconds(ev.tsSeconds)}
                        </span>
                      </div>
                      <span className="text-[10px] font-mono text-gray-500">#ID-{ev.id}</span>
                    </div>

                    <h3 className="text-sm font-bold text-white group-hover:text-indigo-300 transition-colors">
                      {ev.title}
                    </h3>

                    {parsed.description && (
                      <p className="text-xs text-gray-300 leading-relaxed line-clamp-2">
                        {parsed.description}
                      </p>
                    )}
                    {parsed.definition && (
                      <p className="text-xs text-emerald-300/90 leading-relaxed">
                        Def: {parsed.definition}
                      </p>
                    )}
                    {parsed.question && (
                      <p className="text-xs text-amber-300/90 leading-relaxed">
                        Trivia: {parsed.question}
                      </p>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Right Col: Grounded AI Co-Pilot Query Console */}
        <div className="lg:col-span-6 space-y-4">
          <div className="flex items-center space-x-2">
            <Sparkles className="w-5 h-5 text-indigo-400" />
            <h2 className="text-lg font-bold text-white tracking-tight">Grounding Intelligence Console</h2>
          </div>

          <GlassCard className="p-5 flex flex-col h-[600px] justify-between border-white/10">
            {/* Conversation Feed */}
            <div className="flex-1 overflow-y-auto space-y-4 pr-1">
              {conversation.length === 0 ? (
                <div className="h-full flex flex-col items-center justify-center text-center p-6 text-gray-500 space-y-3">
                  <BookOpen className="w-10 h-10 opacity-30 text-indigo-400" />
                  <p className="text-xs max-w-sm">
                    Query the AI Co-Pilot regarding characters, lore, glossary, or scene context at timestamp <strong className="text-white">{formatSeconds(scrubberTime)}</strong>.
                  </p>
                </div>
              ) : (
                conversation.map((msg, idx) => {
                  const isUser = msg.role === 'user';
                  return (
                    <div key={idx} className={`flex flex-col space-y-1 ${isUser ? 'items-end' : 'items-start'}`}>
                      <span className="text-[10px] text-gray-400 px-1 font-semibold">
                        {isUser ? 'You' : 'AI Co-Pilot'}
                      </span>
                      <div
                        className={`max-w-[90%] p-4 rounded-2xl text-xs leading-relaxed ${
                          isUser
                            ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-none'
                            : 'bg-obsidian-950 border border-white/10 text-gray-200 rounded-bl-none space-y-3'
                        }`}
                      >
                        <p>{msg.text}</p>

                        {!isUser && msg.sources && msg.sources.length > 0 && (
                          <div className="pt-2 border-t border-white/10 space-y-1.5">
                            <span className="text-[10px] uppercase font-bold text-indigo-400 tracking-wider block">
                              Verified Authoritative Sources
                            </span>
                            <div className="space-y-1">
                              {msg.sources.map((src, sIdx) => (
                                <div
                                  key={sIdx}
                                  className="p-2 rounded-xl bg-white/5 border border-white/5 text-[10px] text-gray-300"
                                >
                                  <div className="flex justify-between font-mono text-indigo-300 text-[9px] mb-0.5">
                                    <span>[{src.eventType}] {src.title}</span>
                                    <span>#ID-{src.timelineEventId} @ {src.timestamp}s</span>
                                  </div>
                                  <p className="text-gray-400">{src.snippet}</p>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })
              )}

              {isAsking && (
                <div className="flex items-center space-x-2 p-3 bg-white/5 border border-white/5 rounded-2xl animate-pulse">
                  <div className="w-4 h-4 border-2 border-indigo-400 border-t-transparent rounded-full animate-spin" />
                  <span className="text-xs text-indigo-300">Retrieving timeline metadata at {formatSeconds(scrubberTime)}...</span>
                </div>
              )}
            </div>

            {/* Quick Suggested Prompts */}
            <div className="pt-3 border-t border-white/10 space-y-2">
              <span className="text-[10px] uppercase font-bold text-gray-400 tracking-wider">Suggested Prompts</span>
              <div className="flex flex-wrap gap-1.5">
                <button
                  type="button"
                  onClick={() => handleAskQuestion('Who is Thom?')}
                  className="px-2.5 py-1 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-[11px] text-gray-300 transition-colors"
                >
                  👤 Who is Thom?
                </button>
                <button
                  type="button"
                  onClick={() => handleAskQuestion('What is the Neural Drone Uplink?')}
                  className="px-2.5 py-1 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-[11px] text-gray-300 transition-colors"
                >
                  ⚡ Neural Drone Uplink?
                </button>
                <button
                  type="button"
                  onClick={() => handleAskQuestion('What VFX milestone was achieved in this movie?')}
                  className="px-2.5 py-1 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-[11px] text-gray-300 transition-colors"
                >
                  🎬 VFX Milestone?
                </button>
                <button
                  type="button"
                  onClick={() => handleAskQuestion('Who is Darth Vader?')}
                  className="px-2.5 py-1 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-[11px] text-gray-300 transition-colors"
                >
                  🛡️ Test Anti-Hallucination
                </button>
              </div>

              {/* Form Input */}
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleAskQuestion();
                }}
                className="flex items-center space-x-2 pt-1"
              >
                <input
                  type="text"
                  value={questionInput}
                  onChange={(e) => setQuestionInput(e.target.value)}
                  placeholder={`Ask AI Co-Pilot at ${formatSeconds(scrubberTime)}...`}
                  className="flex-1 px-4 py-2.5 bg-obsidian-950 border border-white/20 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <button
                  type="submit"
                  disabled={!questionInput.trim() || isAsking}
                  className="p-2.5 bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 disabled:opacity-40 text-white rounded-xl transition-all shadow-md shadow-indigo-600/30"
                >
                  <Send className="w-4 h-4" />
                </button>
              </form>
            </div>
          </GlassCard>
        </div>

      </div>
    </div>
  );
}
