// src/auth/AuthContext.jsx
import React, { createContext, useState, useEffect, useCallback } from 'react';
import axios from 'axios';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('jwt') || null);
  const [user, setUser] = useState(null);
  const [franchises, setFranchises] = useState([]);

  useEffect(() => {
    const interceptor = axios.interceptors.request.use(config => {
      if (token) config.headers.Authorization = `Bearer ${token}`;
      return config;
    });
    return () => axios.interceptors.request.eject(interceptor);
  }, [token]);

  const login = async (username, password) => {
    const response = await axios.post('/api/auth/login', { username, password });
    const jwt = response.data.token;
    localStorage.setItem('jwt', jwt);
    setToken(jwt);
    return jwt;
  };

const register = async (username, password, recaptchaToken) => {
  await axios.post('/api/auth/register', {
    username,
    password,
    recaptchaToken
  });
};


  const logout = () => {
    localStorage.removeItem('jwt');
    setToken(null);
    setUser(null);
    setFranchises([]);
  };

const fetchFranchises = useCallback(async () => {
  const response = await axios.get('/api/franchise');
  setFranchises(response.data);
  return response.data;
}, []);


const createFranchise = async (data) => {
  try {
    const response = await axios.post('/api/franchise', data);
    setFranchises(prev => [...prev, response.data]);
  } catch (err) {
    console.error("Fehler beim Erstellen eines Franchises:", err);
    throw err; // damit UI reagieren kann
  }
};


useEffect(() => {
  if (token) fetchFranchises();
}, [token]);

  const value = {
    token,
    user,
    login,
    register,
    logout,
    isAuthenticated: !!token,
    franchises,
    fetchFranchises,
    createFranchise,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
