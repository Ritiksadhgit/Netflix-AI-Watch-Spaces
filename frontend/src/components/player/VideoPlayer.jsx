import React, { useRef, useState, useEffect, useCallback } from 'react';

export default function VideoPlayer({
  src,
  poster,
  playbackState = 'PAUSED',
  authoritativePosition = 0.0,
  serverTs = Date.now(),
  clockSkew = 0,
  isHost = false,
  hostDisplayName = 'Host',
  onPlaybackChange,
  onTimeUpdate,
}) {
  const videoRef = useRef(null);
  const containerRef = useRef(null);

  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [bufferedPercent, setBufferedPercent] = useState(0);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showControls, setShowControls] = useState(true);
  const [isBuffering, setIsBuffering] = useState(false);
  const [currentDriftMs, setCurrentDriftMs] = useState(0);
  const [syncStatus, setSyncStatus] = useState('IN_SYNC'); // 'IN_SYNC' | 'ACCELERATING' | 'DECELERATING' | 'RESYNCING'
  const [hostNotice, setHostNotice] = useState('');

  const controlsTimeoutRef = useRef(null);

  // Format time (seconds -> mm:ss or hh:mm:ss)
  const formatTime = (secs) => {
    if (isNaN(secs) || secs < 0) return '00:00';
    const h = Math.floor(secs / 3600);
    const m = Math.floor((secs % 3600) / 60);
    const s = Math.floor(secs % 60);
    if (h > 0) {
      return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  // Participant Micro-Drift Synchronization Algorithm
  useEffect(() => {
    const video = videoRef.current;
    if (!video || isHost) return;

    const isPlaying = playbackState === 'PLAYING';
    const elapsedSinceServerTs = Math.max(0, (Date.now() - serverTs - clockSkew) / 1000);
    const targetPos = isPlaying ? authoritativePosition + elapsedSinceServerTs : authoritativePosition;

    // 1. Play/Pause state synchronization
    if (!isPlaying && !video.paused) {
      video.pause();
      if (Math.abs(video.currentTime - targetPos) > 0.05) {
        video.currentTime = targetPos;
      }
    } else if (isPlaying && video.paused) {
      if (Math.abs(video.currentTime - targetPos) > 0.25) {
        video.currentTime = targetPos;
      }
      video.play().catch((err) => {
        console.warn('Playback autoplay policy prevented play:', err);
      });
    }

    // 2. Continuous drift checking loop
    const syncInterval = setInterval(() => {
      if (!video) return;
      const curTarget = isPlaying
        ? authoritativePosition + Math.max(0, (Date.now() - serverTs - clockSkew) / 1000)
        : authoritativePosition;

      const drift = Math.abs(video.currentTime - curTarget);
      const driftMs = Math.round(drift * 1000);
      setCurrentDriftMs(driftMs);

      if (!isPlaying) {
        if (drift > 0.2) {
          video.currentTime = curTarget;
        }
        video.playbackRate = 1.0;
        setSyncStatus('IN_SYNC');
        return;
      }

      if (drift > 0.25) {
        // Direct gentle seek (< 2s SLA)
        video.currentTime = curTarget;
        video.playbackRate = 1.0;
        setSyncStatus('RESYNCING');
      } else if (drift >= 0.05) {
        // Micro-rate adjustment (50ms - 250ms target)
        if (video.currentTime < curTarget) {
          video.playbackRate = 1.08; // Smooth catch-up
          setSyncStatus('ACCELERATING');
        } else {
          video.playbackRate = 0.92; // Smooth slow-down
          setSyncStatus('DECELERATING');
        }
      } else {
        video.playbackRate = 1.0; // In-sync (< 50ms preferred)
        setSyncStatus('IN_SYNC');
      }
    }, 400);

    return () => clearInterval(syncInterval);
  }, [playbackState, authoritativePosition, serverTs, clockSkew, isHost]);

  // Host Play/Pause toggle
  const togglePlayPause = () => {
    const video = videoRef.current;
    if (!video) return;

    if (!isHost) {
      setHostNotice(`Authoritative playback is controlled by ${hostDisplayName}`);
      setTimeout(() => setHostNotice(''), 3000);
      return;
    }

    if (video.paused) {
      video.play().then(() => {
        if (onPlaybackChange) {
          onPlaybackChange('PLAYING', video.currentTime);
        }
      }).catch((e) => console.error(e));
    } else {
      video.pause();
      if (onPlaybackChange) {
        onPlaybackChange('PAUSED', video.currentTime);
      }
    }
  };

  // Host Seek
  const handleSeek = (e) => {
    if (!isHost) {
      setHostNotice(`Timeline seeking is reserved for the Host (${hostDisplayName})`);
      setTimeout(() => setHostNotice(''), 3000);
      return;
    }

    const video = videoRef.current;
    if (!video || !duration) return;

    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const newRatio = Math.max(0, Math.min(1, clickX / rect.width));
    const newPos = newRatio * duration;

    video.currentTime = newPos;
    setCurrentTime(newPos);
    if (onPlaybackChange) {
      onPlaybackChange(playbackState, newPos);
    }
  };

  // Skip 10 seconds (Host only)
  const handleSkip = (seconds) => {
    if (!isHost) {
      setHostNotice(`Skip controls are reserved for the Host (${hostDisplayName})`);
      setTimeout(() => setHostNotice(''), 3000);
      return;
    }
    const video = videoRef.current;
    if (!video) return;
    const newPos = Math.max(0, Math.min(duration, video.currentTime + seconds));
    video.currentTime = newPos;
    setCurrentTime(newPos);
    if (onPlaybackChange) {
      onPlaybackChange(playbackState, newPos);
    }
  };

  // Volume & Mute
  const handleVolumeChange = (e) => {
    const newVol = parseFloat(e.target.value);
    setVolume(newVol);
    if (videoRef.current) {
      videoRef.current.volume = newVol;
      videoRef.current.muted = newVol === 0;
      setIsMuted(newVol === 0);
    }
  };

  const toggleMute = () => {
    if (videoRef.current) {
      const nextMuted = !isMuted;
      videoRef.current.muted = nextMuted;
      setIsMuted(nextMuted);
      if (nextMuted) {
        videoRef.current.volume = 0;
      } else {
        videoRef.current.volume = volume > 0 ? volume : 0.8;
        setVolume(volume > 0 ? volume : 0.8);
      }
    }
  };

  // Fullscreen
  const toggleFullscreen = () => {
    if (!containerRef.current) return;

    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().catch((err) => console.warn(err));
      setIsFullscreen(true);
    } else {
      document.exitFullscreen().catch((err) => console.warn(err));
      setIsFullscreen(false);
    }
  };

  // Picture in Picture
  const togglePiP = async () => {
    if (!videoRef.current) return;
    try {
      if (document.pictureInPictureElement) {
        await document.exitPictureInPicture();
      } else if (document.pictureInPictureEnabled) {
        await videoRef.current.requestPictureInPicture();
      }
    } catch (err) {
      console.warn('PiP failed', err);
    }
  };

  // Auto-hide controls
  const handleMouseMove = () => {
    setShowControls(true);
    if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current);
    controlsTimeoutRef.current = setTimeout(() => {
      if (playbackState === 'PLAYING') {
        setShowControls(false);
      }
    }, 3500);
  };

  // Video event handlers
  const handleTimeUpdate = () => {
    if (videoRef.current) {
      const cur = videoRef.current.currentTime;
      setCurrentTime(cur);
      if (onTimeUpdate) onTimeUpdate(cur);

      // Buffered progress
      const buf = videoRef.current.buffered;
      if (buf.length > 0 && duration > 0) {
        const bufferedEnd = buf.end(buf.length - 1);
        setBufferedPercent(Math.min(100, (bufferedEnd / duration) * 100));
      }
    }
  };

  const handleLoadedMetadata = () => {
    if (videoRef.current) {
      setDuration(videoRef.current.duration);
    }
  };

  return (
    <div
      ref={containerRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={() => playbackState === 'PLAYING' && setShowControls(false)}
      className="relative w-full aspect-video bg-black rounded-2xl overflow-hidden shadow-2xl select-none group border border-white/10"
    >
      {/* HTML5 Video Element */}
      <video
        ref={videoRef}
        src={src}
        poster={poster}
        playsInline
        preload="metadata"
        onTimeUpdate={handleTimeUpdate}
        onLoadedMetadata={handleLoadedMetadata}
        onWaiting={() => setIsBuffering(true)}
        onPlaying={() => setIsBuffering(false)}
        onClick={togglePlayPause}
        className="w-full h-full object-cover cursor-pointer"
      />

      {/* Buffering Indicator */}
      {isBuffering && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm pointer-events-none">
          <div className="flex flex-col items-center space-y-3">
            <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
            <span className="text-xs uppercase tracking-widest text-indigo-300 font-semibold">Buffering stream</span>
          </div>
        </div>
      )}

      {/* Host Notice Banner for locked actions */}
      {hostNotice && (
        <div className="absolute top-16 left-1/2 transform -translate-x-1/2 bg-amber-500/90 backdrop-blur-md text-obsidian-950 px-4 py-2 rounded-full text-xs font-semibold shadow-lg transition-all animate-bounce flex items-center space-x-2 z-30">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
          <span>{hostNotice}</span>
        </div>
      )}

      {/* Top Header Overlay */}
      <div
        className={`absolute top-0 left-0 right-0 p-4 bg-gradient-to-b from-black/80 via-black/40 to-transparent flex items-center justify-between transition-opacity duration-300 z-20 ${
          showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'
        }`}
      >
        <div className="flex items-center space-x-3">
          {isHost ? (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/20 text-amber-300 border border-amber-500/40">
              <span className="mr-1.5">👑</span> Host Authority Active
            </span>
          ) : (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-white/10 text-gray-300 border border-white/20">
              <span className="mr-1.5">👤</span> Viewer (Host: {hostDisplayName})
            </span>
          )}
        </div>

        {/* Sync SLA Drift Badge */}
        <div className="flex items-center space-x-2">
          {syncStatus === 'IN_SYNC' && (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse mr-1.5" />
              Synced ({currentDriftMs}ms)
            </span>
          )}
          {syncStatus === 'ACCELERATING' && (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-yellow-500/20 text-yellow-400 border border-yellow-500/30">
              <span className="w-2 h-2 rounded-full bg-yellow-400 mr-1.5" />
              Catching up (+8% rate, {currentDriftMs}ms)
            </span>
          )}
          {syncStatus === 'DECELERATING' && (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-yellow-500/20 text-yellow-400 border border-yellow-500/30">
              <span className="w-2 h-2 rounded-full bg-yellow-400 mr-1.5" />
              Slowing down (-8% rate, {currentDriftMs}ms)
            </span>
          )}
          {syncStatus === 'RESYNCING' && (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
              <span className="w-2 h-2 rounded-full bg-indigo-400 animate-ping mr-1.5" />
              Resyncing (&lt;2s)
            </span>
          )}
        </div>
      </div>

      {/* Bottom Controls Bar */}
      <div
        className={`absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/90 via-black/60 to-transparent flex flex-col space-y-2 transition-opacity duration-300 z-20 ${
          showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'
        }`}
      >
        {/* Timeline Scrubber */}
        <div
          onClick={handleSeek}
          className={`relative w-full h-2 group/bar rounded-full cursor-pointer bg-white/20 transition-all ${
            !isHost ? 'cursor-not-allowed' : 'hover:h-3'
          }`}
        >
          {/* Buffered Track */}
          <div
            className="absolute top-0 bottom-0 left-0 bg-white/30 rounded-full"
            style={{ width: `${bufferedPercent}%` }}
          />
          {/* Played Track */}
          <div
            className="absolute top-0 bottom-0 left-0 bg-gradient-to-r from-indigo-500 to-violet-500 rounded-full"
            style={{ width: `${duration > 0 ? (currentTime / duration) * 100 : 0}%` }}
          />
          {/* Scrubber Knob */}
          {isHost && (
            <div
              className="absolute top-1/2 -mt-2 -ml-2 w-4 h-4 bg-white rounded-full shadow-lg border border-indigo-500 opacity-0 group-hover/bar:opacity-100 transition-opacity"
              style={{ left: `${duration > 0 ? (currentTime / duration) * 100 : 0}%` }}
            />
          )}
        </div>

        {/* Control Buttons */}
        <div className="flex items-center justify-between pt-1">
          {/* Left Controls */}
          <div className="flex items-center space-x-3">
            {/* Play/Pause */}
            <button
              onClick={togglePlayPause}
              className={`p-2 rounded-lg text-white hover:text-indigo-400 hover:bg-white/10 transition-colors ${
                !isHost ? 'opacity-60 cursor-pointer' : ''
              }`}
              title={isHost ? (playbackState === 'PLAYING' ? 'Pause' : 'Play') : 'Playback locked to host'}
            >
              {playbackState === 'PLAYING' ? (
                <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
                </svg>
              ) : (
                <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8 5v14l11-7z" />
                </svg>
              )}
            </button>

            {/* Skip 10s Backward (Host only) */}
            {isHost && (
              <button
                onClick={() => handleSkip(-10)}
                className="p-1.5 rounded-lg text-gray-300 hover:text-white hover:bg-white/10 transition-colors"
                title="Rewind 10 seconds"
              >
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12.066 11.2a1 1 0 000 1.6l5.334 4A1 1 0 0019 16V8a1 1 0 00-1.6-.8l-5.334 4zM4.066 11.2a1 1 0 000 1.6l5.334 4A1 1 0 0011 16V8a1 1 0 00-1.6-.8l-5.334 4z" />
                </svg>
              </button>
            )}

            {/* Skip 10s Forward (Host only) */}
            {isHost && (
              <button
                onClick={() => handleSkip(10)}
                className="p-1.5 rounded-lg text-gray-300 hover:text-white hover:bg-white/10 transition-colors"
                title="Fast forward 10 seconds"
              >
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11.933 12.8a1 1 0 000-1.6L6.6 7.2A1 1 0 005 8v8a1 1 0 001.6.8l5.333-4zM19.933 12.8a1 1 0 000-1.6l-5.333-4A1 1 0 0013 8v8a1 1 0 001.6.8l5.333-4z" />
                </svg>
              </button>
            )}

            {/* Volume Control */}
            <div className="flex items-center space-x-2 group/vol">
              <button
                onClick={toggleMute}
                className="p-1.5 rounded-lg text-gray-300 hover:text-white hover:bg-white/10 transition-colors"
                title={isMuted ? 'Unmute' : 'Mute'}
              >
                {isMuted || volume === 0 ? (
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                  </svg>
                )}
              </button>
              <input
                type="range"
                min="0"
                max="1"
                step="0.05"
                value={isMuted ? 0 : volume}
                onChange={handleVolumeChange}
                className="w-16 h-1 accent-indigo-500 bg-white/20 rounded-lg cursor-pointer"
              />
            </div>

            {/* Time Display */}
            <span className="text-xs text-gray-300 font-mono tracking-tight pl-2">
              {formatTime(currentTime)} / {formatTime(duration)}
            </span>
          </div>

          {/* Right Controls */}
          <div className="flex items-center space-x-2">
            {/* Picture in Picture */}
            <button
              onClick={togglePiP}
              className="p-1.5 rounded-lg text-gray-300 hover:text-white hover:bg-white/10 transition-colors"
              title="Picture in Picture"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
            </button>

            {/* Fullscreen */}
            <button
              onClick={toggleFullscreen}
              className="p-1.5 rounded-lg text-gray-300 hover:text-white hover:bg-white/10 transition-colors"
              title={isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'}
            >
              {isFullscreen ? (
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
