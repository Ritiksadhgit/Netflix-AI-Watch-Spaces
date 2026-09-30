-- Seed Data for Netflix AI Watch Spaces

-- Seed Users (Password: Password123! -> BCrypt hash: $2a$10$Dow1hOaTf52l0gJm4g840.vHwE5.Y.680n0B1l4wX2mH2V7oK.s9i)
-- Let's provide a reliable BCrypt hash for Password123!
INSERT IGNORE INTO users (id, email, password_hash, display_name, role, subtitle_locale, avatar_url) VALUES
(1, 'admin@netflixspaces.com', '$2a$10$7R4Q70Nq7eM4KzVv1mFwXeN7eB.H8Q6H1w5Q2B9K3P4T7E1Y0X4W2', 'Chief Admin', 'ADMIN', 'en-US', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80'),
(2, 'host@netflixspaces.com', '$2a$10$7R4Q70Nq7eM4KzVv1mFwXeN7eB.H8Q6H1w5Q2B9K3P4T7E1Y0X4W2', 'Elena (Party Host)', 'HOST', 'en-US', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80'),
(3, 'viewer@netflixspaces.com', '$2a$10$7R4Q70Nq7eM4KzVv1mFwXeN7eB.H8Q6H1w5Q2B9K3P4T7E1Y0X4W2', 'Alex (Viewer)', 'VIEWER', 'en-US', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80');

-- Seed Titles (High Quality Legal Open Assets from Blender Foundation)
INSERT IGNORE INTO titles (id, name, synopsis, duration_seconds, video_asset_url, poster_url, backdrop_url, genres, rating_code, release_year) VALUES
(1, 'Tears of Steel', 'Set in a dystopian future Amsterdam, a desperate group of warriors and scientists battle rogue cybernetic drones while grappling with fractured relationships and past love.', 734, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80', 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1920&q=80', 'Sci-Fi, Cyberpunk, Action', 'PG-13', 2024),
(2, 'Sintel', 'A lonely young tracker named Sintel rescues and nurses a wounded baby dragon, forming an unbreakable bond until an adult dragon kidnaps it, driving her on an epic quest.', 888, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80', 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1920&q=80', 'Fantasy, Adventure, Drama', 'PG-13', 2023),
(3, 'Big Buck Bunny', 'A colossal, benevolent forest rabbit stands up to a gang of mischievous bullies, unleashing hilarious slapstick retribution across the sunny woodland.', 596, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 'https://images.unsplash.com/photo-1535083783855-76ae62b2914e?auto=format&fit=crop&w=600&q=80', 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1920&q=80', 'Animation, Comedy, Family', 'G', 2022);

-- Seed Authored Timeline Events for Title 1 (Tears of Steel)
INSERT IGNORE INTO timeline_events (id, title_id, ts_seconds, event_type, title, payload) VALUES
(1, 1, 15, 'SCENE', 'The Desolate Bridge of Amsterdam', '{"sceneNumber": 1, "description": "The film opens overlooking the Oude Kerk canal under dark, overcast skies as combat debris litters the bridges.", "characters": ["Thom", "Celia"], "location": "Amsterdam Bridge", "keywords": ["dystopia", "ruins", "cybernetics"]}'),
(2, 1, 45, 'CHARACTER', 'Thom (The Lead Pilot)', '{"name": "Thom", "role": "Lead Pilot / Roboticist", "description": "Thom is the leader of the human resistance crew who retrofitted his own robotic hand after the Fall of Amsterdam.", "weapons": ["Heavy Carbine", "Pulse Scanner"]}'),
(3, 1, 95, 'TRIVIA', 'Open Source Visual Effects Breakthrough', '{"question": "Which open-source 3D software was famously battle-tested and refined during the VFX production of this movie?", "options": ["Blender 3D", "Maya", "3ds Max", "Houdini"], "answerIndex": 0, "triviaNote": "Tears of Steel was made using Blender open-source VFX pipeline with tracking and compositing tools developed specifically for this production."}'),
(4, 1, 160, 'GLOSSARY', 'Neural Drone Uplink', '{"term": "Neural Drone Uplink", "definition": "The cranial cybernetic transceiver used by rogue cyborgs to coordinate swarm strikes across city sectors."}'),
(5, 1, 240, 'VARIATION', 'Tactical Maneuver Branch Point', '{"prompt": "The resistance radar detects approaching drone wings. Which defensive tactic should the crew deploy?", "durationSeconds": 20}');

-- Seed Variation Options for Event 5
INSERT IGNORE INTO variation_options (id, timeline_event_id, label, asset_ref, vote_count) VALUES
(1, 5, 'Deploy EMP Pulse Generator', 'branch_emp', 0),
(2, 5, 'Overcharge Sniper Railgun Battery', 'branch_railgun', 0),
(3, 5, 'Scramble Electronic Drone Decoys', 'branch_decoys', 0);

-- Seed Sample Watch Spaces
INSERT IGNORE INTO watch_spaces (id, title_id, host_user_id, name, status, privacy, invite_code, max_participants, is_locked, ai_verbosity, voting_enabled, playback_state, playback_position_seconds) VALUES
('ws_demo_live', 1, 2, 'Cyberpunk Sci-Fi Premiere Watch', 'LIVE', 'PUBLIC', 'CYBER-2026', 50, FALSE, 'NORMAL', TRUE, 'PAUSED', 45.0),
('ws_demo_sintel', 2, 2, 'Fantasy Quest Friday', 'SCHEDULED', 'PUBLIC', 'QUEST-7712', 30, FALSE, 'CHATTY', TRUE, 'PAUSED', 0.0);

-- Seed Sample Participants
INSERT IGNORE INTO watch_space_participants (id, watch_space_id, user_id, is_muted, role_in_room) VALUES
(1, 'ws_demo_live', 2, FALSE, 'HOST'),
(2, 'ws_demo_live', 3, FALSE, 'PARTICIPANT');

-- Seed Sample Chat Messages
INSERT IGNORE INTO chat_messages (id, watch_space_id, user_id, sender_name, msg_type, body, ts_seconds) VALUES
(1, 'ws_demo_live', NULL, 'System', 'SYSTEM', 'Welcome to the Watch Space! Playback is host-synchronized.', 0.0),
(2, 'ws_demo_live', 2, 'Elena (Party Host)', 'USER', 'Welcome everyone! Let me know when you are ready to stream.', 15.0),
(3, 'ws_demo_live', 3, 'Alex (Viewer)', 'USER', 'Sound and video look super crisp! Excited for the AI trivia.', 25.0);

-- Seed Sample Interactions for Recommendation Engine
INSERT IGNORE INTO interactions (id, user_id, title_id, watched_seconds, completed, rating) VALUES
(1, 3, 1, 700, TRUE, 5),
(2, 3, 2, 850, TRUE, 4),
(3, 2, 1, 734, TRUE, 5);
