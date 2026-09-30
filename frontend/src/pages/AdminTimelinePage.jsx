import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { adminTimelineService } from '../services/adminTimelineService';
import { titleService } from '../services/titleService';
import GlassCard from '../components/common/GlassCard';
import CinematicButton from '../components/common/CinematicButton';
import { 
  Shield, 
  ShieldAlert, 
  Film, 
  PlusCircle, 
  Upload, 
  Download, 
  Edit3, 
  Trash2, 
  Layers, 
  Sparkles, 
  BookOpen, 
  UserCheck, 
  GitFork, 
  HelpCircle, 
  Check, 
  AlertCircle,
  X
} from 'lucide-react';

const EVENT_TYPE_CONFIG = {
  SCENE: {
    label: 'Scene',
    color: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    pinColor: 'bg-blue-500',
    icon: Film,
  },
  TRIVIA: {
    label: 'Trivia',
    color: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
    pinColor: 'bg-amber-500',
    icon: Sparkles,
  },
  CHARACTER: {
    label: 'Character',
    color: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
    pinColor: 'bg-purple-500',
    icon: UserCheck,
  },
  GLOSSARY: {
    label: 'Glossary',
    color: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    pinColor: 'bg-emerald-500',
    icon: BookOpen,
  },
  VARIATION: {
    label: 'Variation',
    color: 'bg-rose-500/20 text-rose-400 border-rose-500/30',
    pinColor: 'bg-rose-500',
    icon: GitFork,
  },
};

export default function AdminTimelinePage() {
  const { user } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [titles, setTitles] = useState([]);
  const [selectedTitleId, setSelectedTitleId] = useState(1);
  const [timelineEvents, setTimelineEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('ALL');

  // Selected marker for visual scrubber highlight
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [scrubberHoverTs, setScrubberHoverTs] = useState(null);

  // Modals state
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingMarker, setEditingMarker] = useState(null); // null = create new
  const [showImportModal, setShowImportModal] = useState(false);

  // Load catalog titles
  useEffect(() => {
    titleService.getAllTitles()
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          setTitles(data);
          setSelectedTitleId(data[0].id);
        } else {
          setTitles([
            { id: 1, name: 'Tears of Steel', durationSeconds: 734 },
            { id: 2, name: 'Sintel', durationSeconds: 888 },
            { id: 3, name: 'Big Buck Bunny', durationSeconds: 596 },
          ]);
        }
      })
      .catch(() => {
        setTitles([
          { id: 1, name: 'Tears of Steel', durationSeconds: 734 },
          { id: 2, name: 'Sintel', durationSeconds: 888 },
          { id: 3, name: 'Big Buck Bunny', durationSeconds: 596 },
        ]);
      });
  }, []);

  // Load timeline for selected title
  const fetchTimeline = (titleId) => {
    setLoading(true);
    adminTimelineService.getTimeline(titleId)
      .then((events) => {
        setTimelineEvents(Array.isArray(events) ? events : []);
      })
      .catch((err) => {
        addToast(err.message || 'Failed to load timeline events', 'error');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (selectedTitleId) {
      fetchTimeline(selectedTitleId);
    }
  }, [selectedTitleId]);

  // Access control check
  if (!user || user.role !== 'ADMIN') {
    return (
      <div className="max-w-2xl mx-auto px-4 py-20">
        <GlassCard className="p-8 text-center space-y-5 border-red-500/30 glow-red">
          <div className="w-16 h-16 rounded-full bg-red-500/20 border border-red-500/40 text-red-400 flex items-center justify-center mx-auto text-3xl">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-white tracking-tight">Access Restricted</h2>
            <p className="text-sm text-slate-300 leading-relaxed">
              Timeline Studio requires administrative privileges (<code className="text-red-400">ROLE_ADMIN</code>). Your current account role is <span className="font-semibold text-white">{user?.role || 'UNAUTHENTICATED'}</span>.
            </p>
          </div>
          <CinematicButton
            variant="primary"
            size="md"
            onClick={() => navigate('/dashboard')}
          >
            Return to Dashboard
          </CinematicButton>
        </GlassCard>
      </div>
    );
  }

  const selectedTitle = titles.find((t) => t.id === Number(selectedTitleId)) || { durationSeconds: 734, name: 'Title' };
  const duration = selectedTitle.durationSeconds || 734;

  const formatSeconds = (sec) => {
    if (isNaN(sec) || sec < 0) return '00:00';
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const filteredEvents = timelineEvents.filter((ev) => {
    if (activeFilter === 'ALL') return true;
    return ev.eventType === activeFilter;
  });

  // Delete Marker
  const handleDeleteMarker = async (eventId, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this timeline marker?')) return;
    try {
      await adminTimelineService.deleteMarker(selectedTitleId, eventId);
      addToast('Timeline marker deleted', 'success');
      setTimelineEvents((prev) => prev.filter((ev) => ev.id !== eventId));
      if (selectedEventId === eventId) setSelectedEventId(null);
    } catch (err) {
      addToast(err.message || 'Failed to delete marker', 'error');
    }
  };

  // Open Create/Edit modal
  const handleOpenEdit = (marker = null) => {
    setEditingMarker(marker);
    setShowEditModal(true);
  };

  // Export JSON
  const handleExportJson = () => {
    const jsonStr = JSON.stringify(timelineEvents, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `timeline_title_${selectedTitleId}_${selectedTitle.name.replace(/\s+/g, '_')}.json`;
    link.click();
    URL.revokeObjectURL(url);
    addToast('Timeline exported to JSON file', 'success');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-10">
      {/* Studio Header */}
      <div className="relative rounded-3xl p-8 sm:p-10 glass-panel border-white/10 overflow-hidden glow-indigo">
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <div className="w-14 h-14 rounded-2xl bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shadow-xl">
              <Layers className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  Timeline Studio & Marker Editor
                </h1>
                <span className="text-xs px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/30">
                  ADMIN ONLY
                </span>
              </div>
              <p className="text-xs sm:text-sm text-slate-300 mt-1">
                Visual timeline scrubber, marker authoring, and schema-validated atomic JSON imports
              </p>
            </div>
          </div>

          {/* Title Selector */}
          <div className="flex items-center space-x-3 w-full md:w-auto">
            <label className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Title:</label>
            <select
              value={selectedTitleId}
              onChange={(e) => setSelectedTitleId(Number(e.target.value))}
              className="px-4 py-2.5 rounded-xl bg-obsidian-900 border border-white/15 text-white text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              {titles.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name} ({Math.floor((t.durationSeconds || 0) / 60)}m)
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Visual Timeline Scrubber Canvas */}
      <GlassCard className="p-6 sm:p-8 space-y-6 border-white/10">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <h2 className="text-lg font-bold text-white tracking-tight flex items-center space-x-2">
              <Film className="w-5 h-5 text-indigo-400" />
              <span>Interactive Scrubber ({selectedTitle.name})</span>
            </h2>
            <p className="text-xs text-slate-400">
              Pins show authored markers across the stream. Hover or click a pin to inspect details.
            </p>
          </div>

          <span className="text-xs font-mono text-indigo-300 font-bold px-3 py-1 rounded-full bg-white/5 border border-white/10">
            Total Duration: {formatSeconds(duration)}
          </span>
        </div>

        {/* Scrubber Bar Container */}
        <div className="relative pt-6 pb-2">
          {/* Timeline Bar Track */}
          <div
            className="relative w-full h-4 bg-white/10 rounded-full cursor-crosshair group/scrub hover:h-5 transition-all overflow-visible"
            onMouseMove={(e) => {
              const rect = e.currentTarget.getBoundingClientRect();
              const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
              setScrubberHoverTs(Math.round(pct * duration));
            }}
            onMouseLeave={() => setScrubberHoverTs(null)}
          >
            {/* Hover timestamp tooltip */}
            {scrubberHoverTs !== null && (
              <div
                className="absolute -top-7 -translate-x-1/2 px-2 py-0.5 bg-indigo-600 text-white font-mono text-[10px] font-bold rounded shadow-lg pointer-events-none z-30"
                style={{ left: `${(scrubberHoverTs / duration) * 100}%` }}
              >
                {formatSeconds(scrubberHoverTs)}
              </div>
            )}

            {/* Event Marker Pins */}
            {timelineEvents.map((ev) => {
              const posPercent = Math.max(0, Math.min(100, (ev.tsSeconds / duration) * 100));
              const config = EVENT_TYPE_CONFIG[ev.eventType] || EVENT_TYPE_CONFIG.SCENE;
              const isSelected = selectedEventId === ev.id;

              return (
                <button
                  key={ev.id}
                  onClick={() => setSelectedEventId(ev.id)}
                  title={`[${ev.eventType}] ${ev.title} @ ${formatSeconds(ev.tsSeconds)}`}
                  className={`absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-4 h-4 rounded-full border-2 border-white shadow-lg transition-transform hover:scale-150 z-20 ${config.pinColor} ${
                    isSelected ? 'ring-4 ring-indigo-400 scale-125' : ''
                  }`}
                  style={{ left: `${posPercent}%` }}
                />
              );
            })}
          </div>

          {/* Time Legend */}
          <div className="flex justify-between items-center text-[10px] text-slate-500 font-mono pt-2">
            <span>00:00</span>
            <span>{formatSeconds(Math.floor(duration / 2))}</span>
            <span>{formatSeconds(duration)}</span>
          </div>
        </div>

        {/* Selected Marker Inspector Banner */}
        {selectedEventId && (
          <div className="p-4 rounded-2xl bg-indigo-950/40 border border-indigo-500/40 flex items-start justify-between gap-4 animate-fade-in">
            {(() => {
              const ev = timelineEvents.find((e) => e.id === selectedEventId);
              if (!ev) return null;
              const config = EVENT_TYPE_CONFIG[ev.eventType] || EVENT_TYPE_CONFIG.SCENE;
              const Icon = config.icon;

              return (
                <>
                  <div className="flex items-start space-x-3 min-w-0">
                    <div className={`p-2.5 rounded-xl border ${config.color} shrink-0`}>
                      <Icon className="w-5 h-5" />
                    </div>
                    <div className="space-y-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${config.color}`}>
                          {config.label}
                        </span>
                        <span className="text-xs font-mono text-indigo-300 font-bold">
                          {formatSeconds(ev.tsSeconds)} ({ev.tsSeconds}s)
                        </span>
                      </div>
                      <h4 className="text-sm font-bold text-white truncate">{ev.title}</h4>
                      <pre className="text-[11px] text-slate-300 font-mono bg-black/40 p-2 rounded-lg overflow-x-auto max-h-24">
                        {ev.payloadJson}
                      </pre>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2 shrink-0">
                    <CinematicButton
                      variant="glass"
                      size="sm"
                      onClick={() => handleOpenEdit(ev)}
                      className="text-xs"
                    >
                      <Edit3 className="w-3.5 h-3.5 mr-1" /> Edit
                    </CinematicButton>
                    <button
                      onClick={(e) => handleDeleteMarker(ev.id, e)}
                      className="p-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 transition-colors"
                      title="Delete marker"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => setSelectedEventId(null)}
                      className="text-slate-400 hover:text-white p-1"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                </>
              );
            })()}
          </div>
        )}
      </GlassCard>

      {/* Markers Management Toolbar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        {/* Filter Pills */}
        <div className="flex flex-wrap items-center gap-1.5">
          {['ALL', 'SCENE', 'TRIVIA', 'CHARACTER', 'GLOSSARY', 'VARIATION'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveFilter(tab)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors ${
                activeFilter === tab
                  ? 'bg-indigo-600 text-white shadow-md'
                  : 'bg-white/5 text-slate-400 hover:text-white hover:bg-white/10'
              }`}
            >
              {tab === 'ALL' ? `All Markers (${timelineEvents.length})` : tab}
            </button>
          ))}
        </div>

        {/* Action Buttons */}
        <div className="flex items-center space-x-2.5 w-full sm:w-auto">
          <CinematicButton
            variant="glass"
            size="sm"
            onClick={handleExportJson}
            className="text-xs flex-1 sm:flex-initial"
          >
            <Download className="w-3.5 h-3.5 mr-1.5" />
            <span>Export JSON</span>
          </CinematicButton>

          <CinematicButton
            variant="glass"
            size="sm"
            onClick={() => setShowImportModal(true)}
            className="text-xs flex-1 sm:flex-initial"
          >
            <Upload className="w-3.5 h-3.5 mr-1.5 text-indigo-400" />
            <span>Bulk Import JSON</span>
          </CinematicButton>

          <CinematicButton
            variant="primary"
            size="sm"
            onClick={() => handleOpenEdit(null)}
            className="text-xs flex-1 sm:flex-initial"
          >
            <PlusCircle className="w-3.5 h-3.5 mr-1.5" />
            <span>Add Marker</span>
          </CinematicButton>
        </div>
      </div>

      {/* Markers List */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center space-y-3">
          <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-xs text-slate-400">Loading timeline markers...</span>
        </div>
      ) : filteredEvents.length === 0 ? (
        <GlassCard className="p-12 text-center space-y-4 max-w-lg mx-auto border-white/10">
          <Layers className="w-12 h-12 text-slate-500 mx-auto" />
          <h3 className="text-lg font-bold text-white">No Markers for this Filter</h3>
          <p className="text-xs text-slate-400">
            Create single markers with the editor or bulk-import a pre-authored JSON timeline.
          </p>
          <CinematicButton
            variant="primary"
            size="sm"
            onClick={() => handleOpenEdit(null)}
          >
            Add New Marker
          </CinematicButton>
        </GlassCard>
      ) : (
        <div className="space-y-3">
          {filteredEvents.map((ev) => {
            const config = EVENT_TYPE_CONFIG[ev.eventType] || EVENT_TYPE_CONFIG.SCENE;
            const Icon = config.icon;

            return (
              <GlassCard
                key={ev.id}
                hoverEffect
                className={`p-4 flex items-center justify-between gap-4 border-white/10 cursor-pointer ${
                  selectedEventId === ev.id ? 'border-indigo-400/60 bg-indigo-950/20' : ''
                }`}
                onClick={() => setSelectedEventId(ev.id)}
              >
                <div className="flex items-center space-x-3.5 min-w-0">
                  <div className={`p-2 rounded-xl border ${config.color} shrink-0`}>
                    <Icon className="w-4 h-4" />
                  </div>

                  <div className="space-y-0.5 min-w-0">
                    <div className="flex items-center space-x-2">
                      <span className={`text-[10px] font-bold px-2 py-0.2 rounded-full border ${config.color}`}>
                        {ev.eventType}
                      </span>
                      <span className="text-xs font-mono text-indigo-300 font-bold">
                        {formatSeconds(ev.tsSeconds)} ({ev.tsSeconds}s)
                      </span>
                    </div>
                    <h3 className="text-sm font-bold text-white truncate">{ev.title}</h3>
                  </div>
                </div>

                <div className="flex items-center space-x-2 shrink-0">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleOpenEdit(ev);
                    }}
                    className="p-2 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 hover:text-white transition-colors"
                    title="Edit marker"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={(e) => handleDeleteMarker(ev.id, e)}
                    className="p-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 transition-colors"
                    title="Delete marker"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </GlassCard>
            );
          })}
        </div>
      )}

      {/* Add / Edit Marker Modal */}
      {showEditModal && (
        <AddEditMarkerModal
          isOpen={showEditModal}
          onClose={() => setShowEditModal(false)}
          marker={editingMarker}
          titleId={selectedTitleId}
          durationSeconds={duration}
          onSaved={() => {
            setShowEditModal(false);
            fetchTimeline(selectedTitleId);
          }}
        />
      )}

      {/* Bulk JSON Import Modal */}
      {showImportModal && (
        <BulkImportModal
          isOpen={showImportModal}
          onClose={() => setShowImportModal(false)}
          titleId={selectedTitleId}
          titleName={selectedTitle.name}
          durationSeconds={duration}
          onImported={() => {
            setShowImportModal(false);
            fetchTimeline(selectedTitleId);
          }}
        />
      )}
    </div>
  );
}

// -------------------------------------------------------------
// Add / Edit Marker Modal Component
// -------------------------------------------------------------
function AddEditMarkerModal({ isOpen, onClose, marker, titleId, durationSeconds, onSaved }) {
  const { addToast } = useToast();

  const [tsSeconds, setTsSeconds] = useState(marker ? marker.tsSeconds : 0);
  const [eventType, setEventType] = useState(marker ? marker.eventType : 'SCENE');
  const [title, setTitle] = useState(marker ? marker.title : '');
  const [payloadJson, setPayloadJson] = useState(
    marker ? marker.payloadJson : '{\n  "description": "Scene overview",\n  "keywords": ["sci-fi"]\n}'
  );
  const [jsonError, setJsonError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Validate JSON syntax in real time
  const handlePayloadChange = (e) => {
    const val = e.target.value;
    setPayloadJson(val);
    try {
      JSON.parse(val);
      setJsonError(null);
    } catch (err) {
      setJsonError(err.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      addToast('Event title is required', 'warning');
      return;
    }

    if (jsonError) {
      addToast('Payload must be valid JSON syntax before saving', 'error');
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        tsSeconds: Number(tsSeconds),
        eventType,
        title: title.trim(),
        payloadJson: payloadJson.trim(),
      };

      if (marker && marker.id) {
        await adminTimelineService.updateMarker(titleId, marker.id, payload);
        addToast('Timeline marker updated successfully', 'success');
      } else {
        await adminTimelineService.createMarker(titleId, payload);
        addToast('Timeline marker created successfully', 'success');
      }

      onSaved();
    } catch (err) {
      addToast(err.message || 'Failed to save timeline marker', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-obsidian-950/85 backdrop-blur-md animate-fade-in">
      <GlassCard className="max-w-lg w-full p-6 sm:p-8 space-y-5 border-indigo-500/30 relative">
        <div className="flex items-center justify-between border-b border-white/10 pb-3">
          <h3 className="text-lg font-bold text-white tracking-tight flex items-center space-x-2">
            <Edit3 className="w-5 h-5 text-indigo-400" />
            <span>{marker ? 'Edit Timeline Marker' : 'Add Timeline Marker'}</span>
          </h3>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          {/* Timestamp & Type Row */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                Timestamp (seconds, max {durationSeconds}s):
              </label>
              <input
                type="number"
                min="0"
                max={durationSeconds}
                value={tsSeconds}
                onChange={(e) => setTsSeconds(e.target.value)}
                className="w-full px-3 py-2 bg-obsidian-900 border border-white/15 rounded-xl text-white font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500"
                required
              />
            </div>

            <div>
              <label className="text-slate-300 font-semibold block mb-1">Event Type:</label>
              <select
                value={eventType}
                onChange={(e) => setEventType(e.target.value)}
                className="w-full px-3 py-2 bg-obsidian-900 border border-white/15 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="SCENE">SCENE</option>
                <option value="TRIVIA">TRIVIA</option>
                <option value="CHARACTER">CHARACTER</option>
                <option value="GLOSSARY">GLOSSARY</option>
                <option value="VARIATION">VARIATION</option>
              </select>
            </div>
          </div>

          {/* Event Title */}
          <div>
            <label className="text-slate-300 font-semibold block mb-1">Event Title:</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Thom's Cybernetic Arm Overhaul"
              className="w-full px-3 py-2 bg-obsidian-900 border border-white/15 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
            />
          </div>

          {/* Payload JSON */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-slate-300 font-semibold">Payload JSON Metadata:</label>
              {jsonError ? (
                <span className="text-[10px] text-red-400 flex items-center space-x-1">
                  <AlertCircle className="w-3 h-3" /> <span>Syntax Error</span>
                </span>
              ) : (
                <span className="text-[10px] text-emerald-400 flex items-center space-x-1">
                  <Check className="w-3 h-3" /> <span>Valid JSON</span>
                </span>
              )}
            </div>
            <textarea
              rows={6}
              value={payloadJson}
              onChange={handlePayloadChange}
              className="w-full px-3 py-2 bg-obsidian-900 border border-white/15 rounded-xl text-white font-mono text-[11px] focus:outline-none focus:ring-2 focus:ring-indigo-500 leading-relaxed"
              required
            />
            {jsonError && (
              <p className="text-[10px] text-red-400 mt-1 font-mono">{jsonError}</p>
            )}
          </div>

          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-white/10">
            <CinematicButton
              type="button"
              variant="secondary"
              size="sm"
              onClick={onClose}
            >
              Cancel
            </CinematicButton>
            <CinematicButton
              type="submit"
              variant="primary"
              size="sm"
              disabled={submitting || Boolean(jsonError)}
            >
              {submitting ? 'Saving...' : 'Save Marker'}
            </CinematicButton>
          </div>
        </form>
      </GlassCard>
    </div>
  );
}

// -------------------------------------------------------------
// Bulk JSON Import Modal Component
// -------------------------------------------------------------
function BulkImportModal({ isOpen, onClose, titleId, titleName, durationSeconds, onImported }) {
  const { addToast } = useToast();

  const [jsonText, setJsonText] = useState(
    JSON.stringify([
      {
        tsSeconds: 15,
        eventType: "SCENE",
        title: "The Desolate Bridge of Amsterdam",
        payloadJson: "{\"sceneNumber\": 1, \"description\": \"The film opens overlooking the Oude Kerk canal under dark skies.\"}"
      },
      {
        tsSeconds: 95,
        eventType: "TRIVIA",
        title: "Open Source Visual Effects Breakthrough",
        payloadJson: "{\"question\": \"Which software was used?\", \"options\": [\"Blender\", \"Maya\"], \"answerIndex\": 0}"
      }
    ], null, 2)
  );

  const [replaceExisting, setReplaceExisting] = useState(false);
  const [jsonError, setJsonError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleJsonChange = (e) => {
    const val = e.target.value;
    setJsonText(val);
    try {
      const parsed = JSON.parse(val);
      if (!Array.isArray(parsed)) {
        setJsonError('Top-level JSON payload must be an array of event objects');
      } else {
        setJsonError(null);
      }
    } catch (err) {
      setJsonError(err.message);
    }
  };

  const handleImport = async (e) => {
    e.preventDefault();
    if (jsonError) {
      addToast('JSON must be completely valid before importing', 'error');
      return;
    }

    try {
      setIsSubmitting(true);
      const parsed = JSON.parse(jsonText);
      const res = await adminTimelineService.importTimeline(titleId, parsed, replaceExisting);
      addToast(`Successfully imported ${res.importedCount} markers atomically!`, 'success');
      onImported();
    } catch (err) {
      addToast(err.message || 'Import failed with 422 Unprocessable Entity', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-obsidian-950/85 backdrop-blur-md animate-fade-in">
      <GlassCard className="max-w-xl w-full p-6 sm:p-8 space-y-5 border-indigo-500/30 relative">
        <div className="flex items-center justify-between border-b border-white/10 pb-3">
          <h3 className="text-lg font-bold text-white tracking-tight flex items-center space-x-2">
            <Upload className="w-5 h-5 text-indigo-400" />
            <span>Bulk Import Timeline JSON ({titleName})</span>
          </h3>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleImport} className="space-y-4 text-xs">
          <p className="text-slate-300 leading-relaxed">
            Paste an array of timeline event objects with <code className="text-indigo-300">tsSeconds</code>, <code className="text-indigo-300">eventType</code>, <code className="text-indigo-300">title</code>, and <code className="text-indigo-300">payloadJson</code>. All items undergo atomic validation.
          </p>

          <div className="flex items-center space-x-2">
            <input
              type="checkbox"
              id="replace"
              checked={replaceExisting}
              onChange={(e) => setReplaceExisting(e.target.checked)}
              className="rounded bg-obsidian-900 border-white/20 text-indigo-600 focus:ring-indigo-500"
            />
            <label htmlFor="replace" className="text-slate-300 font-semibold cursor-pointer">
              Replace existing timeline events for this title
            </label>
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-slate-300 font-semibold">JSON Array Payload:</label>
              {jsonError ? (
                <span className="text-[10px] text-red-400 flex items-center space-x-1">
                  <AlertCircle className="w-3 h-3" /> <span>{jsonError}</span>
                </span>
              ) : (
                <span className="text-[10px] text-emerald-400 flex items-center space-x-1">
                  <Check className="w-3 h-3" /> <span>Valid Array Structure</span>
                </span>
              )}
            </div>
            <textarea
              rows={10}
              value={jsonText}
              onChange={handleJsonChange}
              className="w-full px-3 py-2 bg-obsidian-900 border border-white/15 rounded-xl text-white font-mono text-[11px] focus:outline-none focus:ring-2 focus:ring-indigo-500 leading-relaxed"
              required
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-white/10">
            <CinematicButton
              type="button"
              variant="secondary"
              size="sm"
              onClick={onClose}
            >
              Cancel
            </CinematicButton>
            <CinematicButton
              type="submit"
              variant="primary"
              size="sm"
              disabled={isSubmitting || Boolean(jsonError)}
            >
              {isSubmitting ? 'Importing...' : 'Validate & Import'}
            </CinematicButton>
          </div>
        </form>
      </GlassCard>
    </div>
  );
}
