import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { tokenStorage } from '../utils/tokenStorage';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(tokenStorage.getUser());
  const [loading, setLoading] = useState(true);

  const initAuth = useCallback(async () => {
    const token = tokenStorage.getAccessToken();
    if (!token) {
      setLoading(false);
      return;
    }

    try {
      const profile = await authService.getMe();
      setUser(profile);
      tokenStorage.setUser(profile);
    } catch {
      tokenStorage.clearAll();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    initAuth();

    const handleUnauthorized = () => {
      setUser(null);
      tokenStorage.clearAll();
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, [initAuth]);

  const login = async (email, password) => {
    const res = await authService.login({ email, password });
    tokenStorage.setAccessToken(res.accessToken);
    tokenStorage.setRefreshToken(res.refreshToken);
    tokenStorage.setUser(res.user);
    setUser(res.user);
    return res.user;
  };

  const register = async (payload) => {
    const res = await authService.register(payload);
    tokenStorage.setAccessToken(res.accessToken);
    tokenStorage.setRefreshToken(res.refreshToken);
    tokenStorage.setUser(res.user);
    setUser(res.user);
    return res.user;
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      tokenStorage.clearAll();
      setUser(null);
    }
  };

  const updateProfile = async (payload) => {
    const updated = await authService.updateProfile(payload);
    tokenStorage.setUser(updated);
    setUser(updated);
    return updated;
  };

  const isAuthenticated = !!user;
  const isHost = user?.role === 'HOST' || user?.role === 'ADMIN';
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        isHost,
        isAdmin,
        login,
        register,
        logout,
        updateProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
