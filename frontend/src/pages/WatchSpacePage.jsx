import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { watchSpaceService } from '../services/watchSpaceService';
import { apiClient } from '../services/apiClient';
import { useWatchSpaceWebSocket } from '../hooks/useWatchSpaceWebSocket';
import VideoPlayer from '../components/player/VideoPlayer';
import TriviaOverlay from '../components/player/TriviaOverlay';
import NarrativeVotingOverlay from '../components/player/NarrativeVotingOverlay';
import SessionAnalyticsModal from '../components/analytics/SessionAnalyticsModal';
import { interactionService } from '../services/interactionService';
import { tokenStorage } from '../utils/tokenStorage';
import { BarChart3, Copy, Check, Share2 } from 'lucide-react';

export default function WatchSpacePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, token: authToken } = useAuth();
  const token = authToken || tokenStorage.getAccessToken();
  const { addToast } = useToast();
  const currentUserId = user?.id ?? user?.userId;

  const [space, setSpace] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('chat'); // 'chat' | 'roster' | 'ai'
  const [chatInput, setChatInput] = useState('');
  const [aiInput, setAiInput] = useState('');
  const [currentVideoTime, setCurrentVideoTime] = useState(0);
  const [showAnalyticsModal, setShowAnalyticsModal] = useState(false);
  const [codeCopied, setCodeCopied] = useState(false);
  const [inviteCopied, setInviteCopied] = useState(false);

  const [aiHistory, setAiHistory] = useState([
    {
      role: 'assistant',
      text: 'Hello! I am your AI Co-Pilot. As the stream plays, ask me anything about the active scene, characters, or backstory. I answer strictly from verified timeline metadata with traceable citations.',
      sources: [],
      currentScene: 'Introduction',
    }
  ]);

  const chatScrollRef = useRef(null);
  const aiScrollRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  // Handle action rejected callback
  const handleActionRejected = useCallback((payload) => {
    addToast(payload?.reason || 'Action rejected', 'error');
  }, [addToast]);

  // Handle moderation action callback
  const handleModerationAction = useCallback((payload) => {
    if (!payload) return;
    const isTargetMe = currentUserId != null && String(payload.targetUserId) === String(currentUserId);
    if (payload.action === 'MUTE') {
      if (isTargetMe) {
        addToast('You have been muted by the host', 'warning');
      }
    } else if (payload.action === 'UNMUTE') {
      if (isTargetMe) {
        addToast('You have been unmuted by the host', 'success');
      }
    } else if (payload.action === 'HOST_TRANSFERRED') {
      addToast('Host authority has been transferred', 'info');
    } else if (payload.action === 'ROOM_LOCK') {
      addToast(payload.isLocked ? 'Room has been locked by the host' : 'Room has been unlocked', 'info');
    } else if (payload.action === 'KICK') {
      if (isTargetMe) {
        addToast('You have been removed from the Watch Space by the host', 'error');
        navigate('/dashboard');
      }
    }
  }, [currentUserId, addToast, navigate]);

  // WebSocket hook
  const {
    isConnected,
    isConnecting,
    reconnectAttempt,
    playbackState,
    playbackPosition,
    serverTs,
    clockSkew,
    participants,
    participantCount,
    hostUserId,
    isLocked,
    messages,
    setMessages,
    typingUsers,
    activeTrivia,
    dismissTrivia,
    aiAnswer,
    isAiThinking,
    askAiQuestion,
    activeVariation,
    variationResult,
    sendVoteCast,
    sendFinalizeVote,
    dismissVariation,
    dismissVariationResult,
    sendPlaybackUpdate,
    sendChatMessage,
    sendTyping,
    sendModerationAction,
    refreshPresence,
  } = useWatchSpaceWebSocket(id, token, handleActionRejected, handleModerationAction);

  // Refresh presence when switching to roster tab
  useEffect(() => {
    if (isConnected && activeTab === 'roster') {
      refreshPresence();
    }
  }, [isConnected, activeTab, refreshPresence]);

  // Load Watch Space metadata
  useEffect(() => {
    setLoading(true);
    watchSpaceService.getWatchSpace(id)
      .then((data) => {
        setSpace(data);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load Watch Space');
        addToast(err.message || 'Failed to load Watch Space', 'error');
      })
      .finally(() => setLoading(false));
  }, [id, addToast]);

  // Load initial chat history backlog for reconnect & join
  useEffect(() => {
    if (id) {
      apiClient(`/api/v1/watch-spaces/${id}/chat?limit=50`)
        .then((history) => {
          if (Array.isArray(history) && history.length > 0) {
            setMessages(history);
          }
        })
        .catch((err) => console.warn('Could not load chat history', err));
    }
  }, [id, setMessages]);

  // Append new AI answers to AI history
  useEffect(() => {
    if (aiAnswer) {
      setAiHistory((prev) => [
        ...prev,
        {
          role: 'assistant',
          text: aiAnswer.answer,
          sources: aiAnswer.sources || [],
          currentScene: aiAnswer.currentScene,
          timestamp: aiAnswer.timestamp,
        },
      ]);
    }
  }, [aiAnswer]);

  // Auto-scroll chat to bottom
  useEffect(() => {
    if (chatScrollRef.current) {
      chatScrollRef.current.scrollTop = chatScrollRef.current.scrollHeight;
    }
  }, [messages]);

  // Auto-scroll AI co-pilot to bottom
  useEffect(() => {
    if (aiScrollRef.current) {
      aiScrollRef.current.scrollTop = aiScrollRef.current.scrollHeight;
    }
  }, [aiHistory, isAiThinking]);

  // Periodically record playback interaction for user watch history
  useEffect(() => {
    if (!space?.title?.id || currentVideoTime < 5) return;
    const interval = setInterval(() => {
      interactionService.recordInteraction(
        space.title.id,
        Math.floor(currentVideoTime)
      ).catch(() => {});
    }, 30000);

    return () => clearInterval(interval);
  }, [space?.title?.id, currentVideoTime]);

  // Determine if current user is Host
  const currentHostId = hostUserId || space?.hostUserId || space?.hostUser?.id;
  const isHost = Boolean(
    space?.isHost ||
    space?.host ||
    (user && currentHostId != null && currentUserId != null && String(currentUserId) === String(currentHostId)) ||
    user?.role === 'ADMIN'
  );
  const isCurrentUserMuted = participants.find(
    (p) => currentUserId != null && String(p.userId) === String(currentUserId)
  )?.isMuted || false;

  const hostDisplayName = space?.hostUser?.displayName ||
    participants.find((p) => currentHostId != null && String(p.userId) === String(currentHostId))?.displayName ||
    'Host';

  // Chat message submit
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;
    if (isCurrentUserMuted) {
      addToast('You are muted and cannot send messages', 'warning');
      return;
    }

    sendChatMessage(chatInput.trim(), currentVideoTime);
    setChatInput('');
    sendTyping(false);
  };

  // Typing indicator trigger
  const handleChatInputChange = (e) => {
    setChatInput(e.target.value);
    sendTyping(true);
    if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    typingTimeoutRef.current = setTimeout(() => {
      sendTyping(false);
    }, 2000);
  };

  // Ask AI Question submit
  const handleAskAi = (questionText) => {
    const q = questionText || aiInput;
    if (!q.trim()) return;

    setAiHistory((prev) => [
      ...prev,
      {
        role: 'user',
        text: q.trim(),
        timestamp: currentVideoTime,
      }
    ]);
    askAiQuestion(q.trim(), currentVideoTime);
    setAiInput('');
  };

  // Copy Invite Code
  const copyInviteCode = () => {
    if (space?.inviteCode) {
      navigator.clipboard.writeText(space.inviteCode);
      setCodeCopied(true);
      addToast(`Invite code ${space.inviteCode} copied to clipboard!`, 'success');
      setTimeout(() => setCodeCopied(false), 2000);
    }
  };

  // Share or Copy Invite Link
  const handleShareInvite = async () => {
    if (!space) return;
    const inviteUrl = `${window.location.origin}/watch/${space.id}`;
    if (navigator.share) {
      try {
        await navigator.share({
          title: `Join ${space.name} on Netflix AI Watch Spaces`,
          text: `Watch ${space.title?.name || 'with me'} in synchronized stream! Invite code: ${space.inviteCode}`,
          url: inviteUrl,
        });
        addToast('Invite shared!', 'success');
        return;
      } catch (err) {
        if (err.name === 'AbortError') return;
      }
    }

    try {
      await navigator.clipboard.writeText(inviteUrl);
      setInviteCopied(true);
      addToast('Invite link copied to clipboard!', 'success');
      setTimeout(() => setInviteCopied(false), 2000);
    } catch {
      addToast('Failed to copy invite link', 'error');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-obsidian-950 flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-400 font-medium tracking-wide text-sm">Entering Watch Space...</p>
        </div>
      </div>
    );
  }

  if (error || !space) {
    return (
      <div className="min-h-screen bg-obsidian-950 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-obsidian-900 border border-white/10 rounded-2xl p-8 text-center space-y-4">
          <div className="w-16 h-16 mx-auto rounded-full bg-red-500/10 flex items-center justify-center text-red-400">
            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-white">Watch Space Unavailable</h2>
          <p className="text-sm text-gray-400">{error || 'This Watch Space does not exist or has ended.'}</p>
          <Link
            to="/dashboard"
            className="inline-block px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-xl text-sm transition-colors"
          >
            Back to Dashboard
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-obsidian-950 text-white flex flex-col pt-16">
      {/* Reconnect Alert Banner */}
      {!isConnected && reconnectAttempt > 0 && (
        <div className="bg-amber-500/90 backdrop-blur-md text-obsidian-950 px-4 py-2 text-center text-xs font-bold tracking-wide flex items-center justify-center space-x-2 animate-pulse sticky top-16 z-40">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <span>Connection interrupted. Reconnecting to Watch Space... (Attempt {reconnectAttempt}/10)</span>
        </div>
      )}

      {/* Top Header Bar */}
      <header className="bg-obsidian-900/80 backdrop-blur-md border-b border-white/10 px-4 sm:px-6 py-3 flex items-center justify-between z-30">
        <div className="flex items-center space-x-4">
          <Link
            to="/dashboard"
            className="p-2 rounded-xl text-gray-400 hover:text-white hover:bg-white/5 transition-colors"
            title="Return to Dashboard"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </Link>

          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-base sm:text-lg font-bold text-white tracking-tight truncate max-w-xs sm:max-w-md">
                {space.name}
              </h1>
              <span className="text-xs px-2 py-0.5 rounded-full bg-white/10 text-gray-300 font-medium">
                {space.title?.ratingCode || 'PG-13'}
              </span>
              {isLocked && (
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-red-500/20 text-red-300 font-semibold border border-red-500/30">
                  LOCKED
                </span>
              )}
            </div>
            <p className="text-xs text-gray-400 truncate">Streaming: <span className="text-indigo-400 font-semibold">{space.title?.name}</span></p>
          </div>
        </div>

        {/* Space Controls & Metrics */}
        <div className="flex items-center space-x-2 sm:space-x-3">
          {/* Invite Code Button */}
          <button
            onClick={copyInviteCode}
            className={`hidden sm:flex items-center space-x-2 px-3 py-1.5 rounded-xl border text-xs font-mono transition-colors ${
              codeCopied
                ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300'
                : 'bg-white/5 hover:bg-white/10 border-white/10 text-gray-300'
            }`}
            title="Click to copy invite code"
          >
            <span className="text-gray-500">CODE:</span>
            <span className="text-indigo-300 font-semibold">{space.inviteCode}</span>
            {codeCopied ? (
              <span className="flex items-center space-x-1 text-emerald-400 font-semibold">
                <Check className="w-3.5 h-3.5" />
                <span>Copied</span>
              </span>
            ) : (
              <Copy className="w-3.5 h-3.5 text-gray-400" />
            )}
          </button>

          {/* Invite Button */}
          <button
            onClick={handleShareInvite}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-xl border text-xs font-semibold transition-colors ${
              inviteCopied
                ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300'
                : 'bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 border-indigo-500/30'
            }`}
            title="Share or copy invite link"
          >
            {inviteCopied ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-400" />
                <span>Copied</span>
              </>
            ) : (
              <>
                <Share2 className="w-3.5 h-3.5 text-indigo-400" />
                <span>Invite</span>
              </>
            )}
          </button>

          {/* Participant count badge */}
          <div className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>{participantCount || participants.length || 1} online</span>
          </div>

          {/* Session Analytics Button */}
          <button
            onClick={() => setShowAnalyticsModal(true)}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-slate-300 text-xs font-semibold transition-colors"
            title="View Real-Time Room Analytics"
          >
            <BarChart3 className="w-3.5 h-3.5 text-indigo-400" />
            <span className="hidden sm:inline">Analytics</span>
          </button>

          {/* Leave Button */}
          <button
            onClick={() => navigate('/dashboard')}
            className="px-3 py-1.5 rounded-xl bg-red-500/10 hover:bg-red-500/20 text-red-400 text-xs font-semibold border border-red-500/20 transition-colors"
          >
            Leave
          </button>
        </div>
      </header>

      {/* Main Dual-Pane Stage */}
      <main className="flex-1 flex flex-col lg:flex-row overflow-hidden">
        {/* Left Stage: Cinematic Video Player & Title Details */}
        <section className="flex-1 flex flex-col p-4 sm:p-6 overflow-y-auto">
          <div className="w-full max-w-5xl mx-auto space-y-4">
            {/* Player Container with Trivia Overlay */}
            <div className="relative">
              <VideoPlayer
                src={space.title?.videoAssetUrl}
                poster={space.title?.backdropUrl || space.title?.posterUrl}
                titleId={space.title?.id || 1}
                playbackState={playbackState}
                authoritativePosition={playbackPosition}
                serverTs={serverTs}
                clockSkew={clockSkew}
                isHost={isHost}
                hostDisplayName={hostDisplayName}
                onPlaybackChange={(newState, newPos) => sendPlaybackUpdate(newState, newPos)}
                onTimeUpdate={(t) => setCurrentVideoTime(t)}
              />

              {/* Synchronized Scene Trivia Challenge */}
              {activeTrivia && (
                <TriviaOverlay
                  trivia={activeTrivia}
                  onClose={dismissTrivia}
                  onAnswered={(idx, isCorrect) => {
                    if (isCorrect) {
                      addToast('Correct Answer! +100 Trivia Points', 'success');
                    }
                  }}
                />
              )}

              {/* Synchronized Narrative Variation Decision Overlay */}
              {(activeVariation || variationResult) && (
                <NarrativeVotingOverlay
                  variation={activeVariation}
                  result={variationResult}
                  isHost={isHost}
                  onVote={(varId, optId) => {
                    sendVoteCast(varId, optId);
                    addToast('Vote registered!', 'info');
                  }}
                  onFinalize={(varId) => {
                    sendFinalizeVote(varId);
                    addToast('Finalizing narrative decision...', 'info');
                  }}
                  onClose={() => {
                    if (activeVariation) dismissVariation();
                    if (variationResult) dismissVariationResult();
                  }}
                />
              )}
            </div>

            {/* Title Overview Card */}
            <div className="bg-obsidian-900/60 backdrop-blur-md border border-white/10 rounded-2xl p-5 space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <h2 className="text-xl font-bold text-white tracking-tight">{space.title?.name}</h2>
                  <p className="text-xs text-gray-400 mt-1">{space.title?.genres} • {space.title?.releaseYear} • {Math.floor((space.title?.durationSeconds || 0) / 60)} minutes</p>
                </div>
                {isHost && (
                  <span className="px-3 py-1 bg-amber-500/15 border border-amber-500/30 text-amber-300 text-xs font-semibold rounded-full flex items-center space-x-1">
                    <span>👑</span>
                    <span>You are the Host</span>
                  </span>
                )}
              </div>
              <p className="text-sm text-gray-300 leading-relaxed">{space.title?.synopsis}</p>
            </div>
          </div>
        </section>

        {/* Right Stage: Interactive Glassmorphic Drawer (Chat / Presence / AI) */}
        <aside className="w-full lg:w-96 bg-obsidian-900/80 backdrop-blur-md border-t lg:border-t-0 lg:border-l border-white/10 flex flex-col h-96 lg:h-auto">
          {/* Tab Bar */}
          <div className="flex items-center border-b border-white/10 bg-obsidian-950/40">
            <button
              onClick={() => setActiveTab('chat')}
              className={`flex-1 py-3 text-xs font-semibold tracking-wider uppercase transition-colors border-b-2 flex items-center justify-center space-x-1.5 ${
                activeTab === 'chat'
                  ? 'text-indigo-400 border-indigo-500 bg-white/5'
                  : 'text-gray-400 border-transparent hover:text-white'
              }`}
            >
              <span>Chat</span>
              <span className="text-[10px] px-1.5 py-0.2 bg-white/10 rounded-full">{messages.length}</span>
            </button>

            <button
              onClick={() => setActiveTab('roster')}
              className={`flex-1 py-3 text-xs font-semibold tracking-wider uppercase transition-colors border-b-2 flex items-center justify-center space-x-1.5 ${
                activeTab === 'roster'
                  ? 'text-indigo-400 border-indigo-500 bg-white/5'
                  : 'text-gray-400 border-transparent hover:text-white'
              }`}
            >
              <span>People</span>
              <span className="text-[10px] px-1.5 py-0.2 bg-white/10 rounded-full">{participantCount || participants.length}</span>
            </button>

            <button
              onClick={() => setActiveTab('ai')}
              className={`flex-1 py-3 text-xs font-semibold tracking-wider uppercase transition-colors border-b-2 flex items-center justify-center space-x-1.5 ${
                activeTab === 'ai'
                  ? 'text-indigo-400 border-indigo-500 bg-white/5'
                  : 'text-gray-400 border-transparent hover:text-white'
              }`}
            >
              <span>AI Co-Pilot</span>
              <span className="w-2 h-2 rounded-full bg-indigo-400 animate-pulse" />
            </button>
          </div>

          {/* Tab 1: Live Chat */}
          {activeTab === 'chat' && (
            <div className="flex-1 flex flex-col justify-between overflow-hidden p-3">
              {/* Message List */}
              <div ref={chatScrollRef} className="flex-1 overflow-y-auto space-y-3 pr-1">
                {messages.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-center p-4 text-gray-500">
                    <svg className="w-8 h-8 mb-2 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                    </svg>
                    <p className="text-xs">No messages yet. Say hello to everyone watching!</p>
                  </div>
                ) : (
                  messages.map((m, idx) => {
                    const isSystem = m.msgType === 'SYSTEM';
                    const isSenderHost = currentHostId != null && String(m.userId) === String(currentHostId);
                    const isMe = currentUserId != null && String(m.userId) === String(currentUserId);

                    if (isSystem) {
                      return (
                        <div key={m.id || idx} className="text-center my-2">
                          <span className="inline-block px-3 py-1 rounded-full text-[11px] bg-white/5 text-gray-400 border border-white/5">
                            {m.body}
                          </span>
                        </div>
                      );
                    }

                    return (
                      <div key={m.id || idx} className={`flex flex-col space-y-1 ${isMe ? 'items-end' : 'items-start'}`}>
                        <div className="flex items-center space-x-1.5 text-[11px] text-gray-400 px-1">
                          <span className={`font-semibold ${isMe ? 'text-indigo-400' : 'text-gray-300'}`}>
                            {m.senderName}
                          </span>
                          {isSenderHost && (
                            <span className="text-[9px] px-1.5 py-0.2 rounded bg-amber-500/20 text-amber-300 font-semibold">
                              HOST
                            </span>
                          )}
                        </div>
                        <div
                          className={`max-w-[85%] px-3.5 py-2 rounded-2xl text-xs leading-relaxed break-words ${
                            isMe
                              ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-none shadow-md shadow-indigo-600/20'
                              : 'bg-white/10 text-gray-200 rounded-bl-none border border-white/5'
                          }`}
                        >
                          {m.body}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              {/* Typing users alert */}
              {Object.keys(typingUsers).length > 0 && (
                <div className="text-[10px] text-gray-400 italic px-2 py-1 animate-pulse">
                  {Object.values(typingUsers).join(', ')} is typing...
                </div>
              )}

              {/* Message Input Box */}
              <form onSubmit={handleSendMessage} className="mt-2 pt-2 border-t border-white/10 flex items-center space-x-2">
                <input
                  type="text"
                  value={chatInput}
                  onChange={handleChatInputChange}
                  disabled={isCurrentUserMuted}
                  placeholder={isCurrentUserMuted ? 'You are muted by the host' : 'Send a message...'}
                  className="flex-1 px-3.5 py-2 bg-obsidian-950 border border-white/15 rounded-xl text-white placeholder-gray-500 text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50"
                />
                <button
                  type="submit"
                  disabled={isCurrentUserMuted || !chatInput.trim()}
                  className="p-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white rounded-xl transition-colors shadow-md shadow-indigo-600/30"
                  title="Send message"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                </button>
              </form>
            </div>
          )}

          {/* Tab 2: Participants Roster & Host Moderation */}
          {activeTab === 'roster' && (
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-white/10">
                <span className="text-xs font-bold uppercase tracking-wider text-gray-400">Connected ({participants.length})</span>
                {isHost && (
                  <button
                    onClick={() => sendModerationAction('LOCK', null, { locked: !isLocked })}
                    className={`px-2.5 py-1 rounded-lg text-[10px] font-semibold border transition-colors ${
                      isLocked
                        ? 'bg-red-500/20 border-red-500/40 text-red-300'
                        : 'bg-white/5 border-white/10 text-gray-300 hover:text-white'
                    }`}
                  >
                    {isLocked ? 'Unlock Room' : 'Lock Room'}
                  </button>
                )}
              </div>

              <div className="space-y-2">
                {participants.map((p) => {
                  const participantIsHost = currentHostId != null && String(p.userId) === String(currentHostId);
                  const isMe = currentUserId != null && String(p.userId) === String(currentUserId);

                  return (
                    <div
                      key={p.userId}
                      className="flex items-center justify-between p-2.5 rounded-xl bg-white/5 border border-white/5 hover:border-white/10 transition-colors"
                    >
                      <div className="flex items-center space-x-2.5">
                        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center text-xs font-bold text-white shadow-inner">
                          {p.displayName ? p.displayName[0].toUpperCase() : 'U'}
                        </div>
                        <div>
                          <div className="flex items-center space-x-1.5">
                            <span className="text-xs font-semibold text-gray-200">
                              {p.displayName} {isMe && '(You)'}
                            </span>
                            {participantIsHost && (
                              <span className="text-[10px] text-amber-400 font-bold" title="Host">👑</span>
                            )}
                            {p.isMuted && (
                              <span className="text-[9px] px-1 rounded bg-red-500/20 text-red-300 font-medium">Muted</span>
                            )}
                          </div>
                          <span className="text-[10px] text-gray-400">{p.role}</span>
                        </div>
                      </div>

                      {/* Moderation actions (Visible only to Host for other participants) */}
                      {isHost && !isMe && (
                        <div className="flex items-center space-x-1">
                          {/* Mute/Unmute */}
                          <button
                            onClick={() => sendModerationAction(p.isMuted ? 'UNMUTE' : 'MUTE', p.userId)}
                            className="p-1 rounded text-gray-400 hover:text-white hover:bg-white/10 text-xs"
                            title={p.isMuted ? 'Unmute participant' : 'Mute participant'}
                          >
                            {p.isMuted ? '🔊' : '🔇'}
                          </button>

                          {/* Transfer Host */}
                          <button
                            onClick={() => {
                              if (window.confirm(`Transfer host authority to ${p.displayName}?`)) {
                                sendModerationAction('TRANSFER_HOST', p.userId);
                              }
                            }}
                            className="p-1 rounded text-gray-400 hover:text-amber-400 hover:bg-white/10 text-xs"
                            title="Make room host"
                          >
                            👑
                          </button>

                          {/* Kick */}
                          <button
                            onClick={() => {
                              if (window.confirm(`Remove ${p.displayName} from this Watch Space?`)) {
                                sendModerationAction('KICK', p.userId);
                              }
                            }}
                            className="p-1 rounded text-gray-400 hover:text-red-400 hover:bg-white/10 text-xs"
                            title="Remove participant"
                          >
                            ✕
                          </button>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Tab 3: Interactive Grounded AI Co-Pilot */}
          {activeTab === 'ai' && (
            <div className="flex-1 flex flex-col justify-between overflow-hidden p-3">
              {/* AI Conversation Scroll View */}
              <div ref={aiScrollRef} className="flex-1 overflow-y-auto space-y-3.5 pr-1">
                {/* Active Context Banner */}
                <div className="p-3 bg-gradient-to-r from-indigo-500/10 via-violet-500/10 to-indigo-500/10 border border-indigo-500/20 rounded-xl space-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <span className="w-2 h-2 rounded-full bg-indigo-400 animate-pulse" />
                      <span className="text-xs font-bold text-indigo-300">Retrieval Grounded Context</span>
                    </div>
                    <span className="text-[10px] font-mono text-gray-400">
                      Sync: {Math.floor(currentVideoTime)}s
                    </span>
                  </div>
                  <p className="text-[11px] text-gray-300 leading-relaxed">
                    Answers are strictly bound to authored timeline metadata within [ts - 90s, ts + 30s].
                  </p>
                </div>

                {/* AI Q&A Feed */}
                {aiHistory.map((item, idx) => {
                  const isUser = item.role === 'user';

                  return (
                    <div key={idx} className={`flex flex-col space-y-1.5 ${isUser ? 'items-end' : 'items-start'}`}>
                      <div className="flex items-center space-x-1.5 text-[11px] text-gray-400 px-1">
                        <span className="font-semibold text-indigo-300">
                          {isUser ? 'You' : 'AI Co-Pilot'}
                        </span>
                        {item.currentScene && (
                          <span className="text-[10px] text-gray-400 font-mono">
                            • {item.currentScene}
                          </span>
                        )}
                      </div>

                      <div
                        className={`max-w-[90%] p-3.5 rounded-2xl text-xs leading-relaxed ${
                          isUser
                            ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-none shadow-md'
                            : 'bg-obsidian-950/90 border border-white/10 text-gray-200 rounded-bl-none space-y-2'
                        }`}
                      >
                        <p>{item.text}</p>

                        {/* Traceable Source Citations Accordion */}
                        {!isUser && item.sources && item.sources.length > 0 && (
                          <div className="pt-2 border-t border-white/10 space-y-1">
                            <span className="text-[10px] uppercase font-bold text-indigo-400 tracking-wider block">
                              Verified Timeline Citations
                            </span>
                            <div className="flex flex-col space-y-1">
                              {item.sources.map((src, sIdx) => (
                                <div
                                  key={sIdx}
                                  className="p-1.5 rounded-lg bg-white/5 border border-white/5 text-[10px] text-gray-300 flex flex-col space-y-0.5"
                                >
                                  <div className="flex items-center justify-between font-mono text-[9px] text-indigo-300">
                                    <span>[{src.eventType}] {src.title}</span>
                                    <span>ID #{src.timelineEventId} @ {src.timestamp}s</span>
                                  </div>
                                  {src.snippet && (
                                    <p className="text-gray-400 text-[10px] line-clamp-2">{src.snippet}</p>
                                  )}
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}

                {/* AI Thinking Skeleton State */}
                {isAiThinking && (
                  <div className="flex items-center space-x-2 p-3 bg-white/5 border border-white/5 rounded-2xl animate-pulse">
                    <div className="w-5 h-5 rounded-full border-2 border-indigo-400 border-t-transparent animate-spin" />
                    <span className="text-xs text-indigo-300 font-medium">
                      Retrieving timeline events at {Math.floor(currentVideoTime)}s...
                    </span>
                  </div>
                )}
              </div>

              {/* Suggested Questions Quick Chips */}
              <div className="mt-2 pt-2 border-t border-white/10 space-y-1.5">
                <span className="text-[10px] uppercase font-bold text-gray-400 tracking-wider">Suggested Questions</span>
                <div className="flex flex-wrap gap-1.5">
                  <button
                    type="button"
                    onClick={() => handleAskAi('Who is Thom?')}
                    className="px-2.5 py-1 bg-white/5 hover:bg-white/10 border border-white/10 rounded-full text-[11px] text-gray-300 transition-colors"
                  >
                    👤 Who is Thom?
                  </button>
                  <button
                    type="button"
                    onClick={() => handleAskAi('What is the Neural Drone Uplink?')}
                    className="px-2.5 py-1 bg-white/5 hover:bg-white/10 border border-white/10 rounded-full text-[11px] text-gray-300 transition-colors"
                  >
                    ⚡ Neural Drone Uplink?
                  </button>
                  <button
                    type="button"
                    onClick={() => handleAskAi('What software VFX milestone was achieved?')}
                    className="px-2.5 py-1 bg-white/5 hover:bg-white/10 border border-white/10 rounded-full text-[11px] text-gray-300 transition-colors"
                  >
                    🎬 VFX Breakthrough?
                  </button>
                </div>
              </div>

              {/* AI Question Input Form */}
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleAskAi();
                }}
                className="mt-2 pt-2 flex items-center space-x-2"
              >
                <input
                  type="text"
                  value={aiInput}
                  onChange={(e) => setAiInput(e.target.value)}
                  placeholder="Ask about this scene or characters..."
                  className="flex-1 px-3.5 py-2 bg-obsidian-950 border border-white/15 rounded-xl text-white placeholder-gray-500 text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <button
                  type="submit"
                  disabled={!aiInput.trim() || isAiThinking}
                  className="p-2 bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 disabled:opacity-40 text-white rounded-xl transition-all shadow-md shadow-indigo-600/30"
                  title="Ask AI Co-Pilot"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </button>
              </form>
            </div>
          )}
        </aside>
      </main>

      {/* Session Performance & Community Analytics Modal */}
      <SessionAnalyticsModal
        isOpen={showAnalyticsModal}
        onClose={() => setShowAnalyticsModal(false)}
        watchSpaceId={id}
        spaceName={space?.name}
      />
    </div>
  );
}
