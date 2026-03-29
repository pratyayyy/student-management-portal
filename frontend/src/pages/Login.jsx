import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Login() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('login'); // 'login' | 'register'
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [regForm, setRegForm] = useState({
    username: '', password: '', confirmPassword: '', role: 'ADMIN', studentId: ''
  });

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login(loginForm.username, loginForm.password);
      navigate(user.role === 'ADMIN' ? '/home' : '/student/home');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    if (regForm.password !== regForm.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (regForm.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }
    if (regForm.username.length < 3) {
      setError('Username must be at least 3 characters');
      return;
    }
    if (regForm.role === 'STUDENT' && !regForm.studentId) {
      setError('Student ID is required for Student role');
      return;
    }
    setLoading(true);
    try {
      await register(regForm);
      setSuccess('Account created! Please log in.');
      setTab('login');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-600 via-violet-600 to-purple-700 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white/20 rounded-2xl mb-4 backdrop-blur-sm">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M12 14l9-5-9-5-9 5 9 5z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z" />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-white">Student Portal</h1>
          <p className="text-indigo-200 mt-1">Management System</p>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl overflow-hidden">
          {/* Tabs */}
          <div className="flex border-b border-slate-100">
            {['login', 'register'].map(t => (
              <button
                key={t}
                onClick={() => { setTab(t); setError(''); setSuccess(''); }}
                className={`flex-1 py-4 text-sm font-semibold transition capitalize ${
                  tab === t
                    ? 'text-indigo-600 border-b-2 border-indigo-600'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                {t === 'login' ? '🔐 Sign In' : '✨ Create Account'}
              </button>
            ))}
          </div>

          <div className="p-8">
            {error && (
              <div className="mb-4 px-4 py-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
                ⚠️ {error}
              </div>
            )}
            {success && (
              <div className="mb-4 px-4 py-3 bg-green-50 border border-green-200 text-green-700 rounded-lg text-sm">
                ✅ {success}
              </div>
            )}

            {tab === 'login' ? (
              <form onSubmit={handleLogin} className="space-y-5">
                <div>
                  <label className="label">Username</label>
                  <input
                    type="text" required
                    value={loginForm.username}
                    onChange={e => setLoginForm(f => ({ ...f, username: e.target.value }))}
                    placeholder="Enter your username"
                    className="input"
                  />
                </div>
                <div>
                  <label className="label">Password</label>
                  <input
                    type="password" required
                    value={loginForm.password}
                    onChange={e => setLoginForm(f => ({ ...f, password: e.target.value }))}
                    placeholder="Enter your password"
                    className="input"
                  />
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-2.5">
                  {loading ? 'Signing in...' : 'Sign In →'}
                </button>
              </form>
            ) : (
              <form onSubmit={handleRegister} className="space-y-4">
                <div>
                  <label className="label">Username</label>
                  <input
                    type="text" required
                    value={regForm.username}
                    onChange={e => setRegForm(f => ({ ...f, username: e.target.value }))}
                    placeholder="Min. 3 characters"
                    className="input"
                  />
                </div>
                <div>
                  <label className="label">Role</label>
                  <select
                    value={regForm.role}
                    onChange={e => setRegForm(f => ({ ...f, role: e.target.value }))}
                    className="input"
                  >
                    <option value="ADMIN">Admin</option>
                    <option value="STUDENT">Student</option>
                  </select>
                </div>
                {regForm.role === 'STUDENT' && (
                  <div>
                    <label className="label">Student ID</label>
                    <input
                      type="text" required
                      value={regForm.studentId}
                      onChange={e => setRegForm(f => ({ ...f, studentId: e.target.value }))}
                      placeholder="e.g. 2024-0001"
                      className="input"
                    />
                  </div>
                )}
                <div>
                  <label className="label">Password</label>
                  <input
                    type="password" required
                    value={regForm.password}
                    onChange={e => setRegForm(f => ({ ...f, password: e.target.value }))}
                    placeholder="Min. 6 characters"
                    className="input"
                  />
                </div>
                <div>
                  <label className="label">Confirm Password</label>
                  <input
                    type="password" required
                    value={regForm.confirmPassword}
                    onChange={e => setRegForm(f => ({ ...f, confirmPassword: e.target.value }))}
                    placeholder="Repeat password"
                    className="input"
                  />
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-2.5">
                  {loading ? 'Creating...' : 'Create Account →'}
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
