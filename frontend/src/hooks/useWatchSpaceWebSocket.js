import { useState, useEffect, useRef, useCallback } from 'react';

export function useWatchSpaceWebSocket(watchSpaceId, token, onActionRejected, onModerationAction) {
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(true);
  const [reconnectAttempt, setReconnectAttempt] = useState(0);

  // Playback state
  const [playbackState, setPlaybackState] = useState('PAUSED');
  const [playbackPosition, setPlaybackPosition] = useState(0.0);
  const [serverTs, setServerTs] = useState(Date.now());
  const [clockSkew, setClockSkew] = useState(0);
  const [rtt, setRtt] = useState(0);

  // Room presence & info
  const [participants, setParticipants] = useState([]);
  const [participantCount, setParticipantCount] = useState(0);
  const [hostUserId, setHostUserId] = useState(null);
  const [isLocked, setIsLocked] = useState(false);

  // Chat
  const [messages, setMessages] = useState([]);
  const [typingUsers, setTypingUsers] = useState({});

  // Trivia & AI Co-Pilot
  const [activeTrivia, setActiveTrivia] = useState(null);
  const [aiAnswer, setAiAnswer] = useState(null);
  const [isAiThinking, setIsAiThinking] = useState(false);

  // Narrative Variation Voting
  const [activeVariation, setActiveVariation] = useState(null);
  const [variationResult, setVariationResult] = useState(null);

  const socketRef = useRef(null);

  const pingIntervalRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const isManuallyClosedRef = useRef(false);

  // Send envelope helper
  const sendEnvelope = useCallback((event, payload = {}) => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      const envelope = {
        event,
        watchSpaceId,
        payload,
        ts: Date.now(),
      };
      socketRef.current.send(JSON.stringify(envelope));
    }
  }, [watchSpaceId]);

  // Connect to WebSocket
  const connect = useCallback(() => {
    if (!watchSpaceId || !token) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/ws/watch-space?token=${encodeURIComponent(token)}&watchSpaceId=${encodeURIComponent(watchSpaceId)}`;

    setIsConnecting(true);
    const ws = new WebSocket(wsUrl);
    socketRef.current = ws;

    ws.onopen = () => {
      setIsConnected(true);
      setIsConnecting(false);
      setReconnectAttempt(0);

      // Start ping heartbeat for drift calculation
      if (pingIntervalRef.current) clearInterval(pingIntervalRef.current);
      pingIntervalRef.current = setInterval(() => {
        sendEnvelope('room.sync.ping', { clientSendTs: Date.now() });
      }, 5000);

      // Immediate initial ping
      sendEnvelope('room.sync.ping', { clientSendTs: Date.now() });
    };

    ws.onmessage = (event) => {
      try {
        const envelope = JSON.parse(event.data);
        const { event: evtType, payload } = envelope;

        if (evtType === 'room.playback.snapshot') {
          if (payload) {
            setPlaybackState(payload.state || 'PAUSED');
            setPlaybackPosition(payload.position || 0.0);
            setServerTs(payload.serverTs || Date.now());
            if (payload.hostUserId) setHostUserId(payload.hostUserId);
            if (payload.isLocked !== undefined) setIsLocked(payload.isLocked);
          }
        } else if (evtType === 'room.playback.update') {
          if (payload) {
            setPlaybackState(payload.state || 'PAUSED');
            setPlaybackPosition(payload.position || 0.0);
            setServerTs(payload.serverTs || Date.now());
          }
        } else if (evtType === 'room.presence.update') {
          if (payload) {
            setParticipants(payload.participants || []);
            setParticipantCount(payload.count || (payload.participants ? payload.participants.length : 0));
            if (payload.hostUserId) setHostUserId(payload.hostUserId);
            if (payload.isLocked !== undefined) setIsLocked(payload.isLocked);
          }
        } else if (evtType === 'room.sync.pong') {
          if (payload) {
            const now = Date.now();
            const measuredRtt = now - (payload.clientSendTs || now);
            const measuredSkew = (payload.serverTs || now) - (payload.clientSendTs || now) - (measuredRtt / 2);
            setRtt((prev) => (prev === 0 ? measuredRtt : Math.round(prev * 0.6 + measuredRtt * 0.4)));
            setClockSkew((prev) => (prev === 0 ? measuredSkew : Math.round(prev * 0.6 + measuredSkew * 0.4)));
          }
        } else if (evtType === 'room.chat.message') {
          if (payload) {
            setMessages((prev) => [...prev, payload]);
          }
        } else if (evtType === 'room.chat.typing') {
          if (payload && payload.userId) {
            setTypingUsers((prev) => {
              if (payload.isTyping) {
                return { ...prev, [payload.userId]: payload.displayName || 'Someone' };
              } else {
                const next = { ...prev };
                delete next[payload.userId];
                return next;
              }
            });
          }
        } else if (evtType === 'room.ai.trivia') {
          if (payload) {
            setActiveTrivia(payload);
          }
        } else if (evtType === 'room.ai.answer') {
          if (payload) {
            setAiAnswer(payload);
            setIsAiThinking(false);
          }
        } else if (evtType === 'room.action.rejected') {
          setIsAiThinking(false);
          if (onActionRejected) {
            onActionRejected(payload);
          }
        } else if (evtType === 'room.moderation.action') {
          if (onModerationAction) {
            onModerationAction(payload);
          }
        } else if (evtType === 'room.variation.voteOpen') {
          if (payload) {
            setActiveVariation(payload);
            setVariationResult(null);
          }
        } else if (evtType === 'room.variation.voteCast') {
          if (payload) {
            setActiveVariation((prev) => {
              if (!prev) return prev;
              const incomingOpts = payload.options || [];
              const updatedOptions = (prev.options || []).map((opt) => {
                const match = incomingOpts.find((o) => o.id === opt.id);
                return match ? { ...opt, votes: match.votes } : opt;
              });
              return {
                ...prev,
                totalVotes: payload.totalVotes !== undefined ? payload.totalVotes : prev.totalVotes,
                options: updatedOptions,
              };
            });
          }
        } else if (evtType === 'room.variation.applied') {
          if (payload) {
            setVariationResult(payload);
            setActiveVariation(null);
          }
        }
      } catch (err) {
        console.error('Failed to parse incoming WebSocket message', err);
      }
    };

    ws.onerror = (err) => {
      console.warn('WebSocket encountered error', err);
    };

    ws.onclose = (e) => {
      setIsConnected(false);
      setIsConnecting(false);
      if (pingIntervalRef.current) clearInterval(pingIntervalRef.current);

      if (!isManuallyClosedRef.current) {
        setReconnectAttempt((prev) => {
          const nextAttempt = prev + 1;
          const delay = Math.min(1000 * Math.pow(1.5, prev), 10000);
          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, delay);
          return nextAttempt;
        });
      }
    };
  }, [watchSpaceId, token, sendEnvelope, onActionRejected, onModerationAction]);

  useEffect(() => {
    isManuallyClosedRef.current = false;
    connect();

    return () => {
      isManuallyClosedRef.current = true;
      if (pingIntervalRef.current) clearInterval(pingIntervalRef.current);
      if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
      if (socketRef.current) socketRef.current.close();
    };
  }, [connect]);

  // Actions
  const sendPlaybackUpdate = useCallback((state, position) => {
    sendEnvelope('room.playback.update', {
      state,
      position: Math.max(0, position),
    });
  }, [sendEnvelope]);

  const sendChatMessage = useCallback((body, tsSeconds = 0.0) => {
    sendEnvelope('room.chat.message', {
      body,
      tsSeconds,
    });
  }, [sendEnvelope]);

  const sendTyping = useCallback((isTyping) => {
    sendEnvelope('room.chat.typing', {
      isTyping,
    });
  }, [sendEnvelope]);

  const sendModerationAction = useCallback((action, targetUserId, options = {}) => {
    sendEnvelope('room.moderation.action', {
      action,
      targetUserId,
      ...options,
    });
  }, [sendEnvelope]);

  const askAiQuestion = useCallback((question, currentTimestamp = 0.0) => {
    setIsAiThinking(true);
    sendEnvelope('room.ai.ask', {
      question,
      currentTimestamp,
    });
  }, [sendEnvelope]);

  const dismissTrivia = useCallback(() => {
    setActiveTrivia(null);
  }, []);

  const sendVoteCast = useCallback((variationId, optionId) => {
    sendEnvelope('room.variation.voteCast', {
      variationId,
      optionId,
    });
  }, [sendEnvelope]);

  const sendFinalizeVote = useCallback((variationId) => {
    sendEnvelope('room.variation.finalize', {
      variationId,
    });
  }, [sendEnvelope]);

  const dismissVariation = useCallback(() => {
    setActiveVariation(null);
  }, []);

  const dismissVariationResult = useCallback(() => {
    setVariationResult(null);
  }, []);

  return {
    isConnected,
    isConnecting,
    reconnectAttempt,
    playbackState,
    playbackPosition,
    serverTs,
    clockSkew,
    rtt,
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
  };
}
