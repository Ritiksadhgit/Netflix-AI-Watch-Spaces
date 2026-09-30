import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';
import ProtectedRoute from './components/auth/ProtectedRoute';

import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import WatchSpacePage from './pages/WatchSpacePage';
import AiCoPilotPage from './pages/AiCoPilotPage';
import NotFoundPage from './pages/NotFoundPage';

function AppLayout() {
  const location = useLocation();
  const isWatchRoom = location.pathname.startsWith('/watch/');

  return (
    <div className="flex flex-col min-h-screen bg-obsidian-950">
      {!isWatchRoom && <Navbar />}
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/ai-copilot"
            element={
              <ProtectedRoute>
                <AiCoPilotPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/watch/:id"
            element={
              <ProtectedRoute>
                <WatchSpacePage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      {!isWatchRoom && <Footer />}
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <ToastProvider>
        <AuthProvider>
          <AppLayout />
        </AuthProvider>
      </ToastProvider>
    </Router>
  );
}
