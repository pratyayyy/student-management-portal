import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function NotFound() {
  const { user } = useAuth();
  const home = user?.role === 'ADMIN' ? '/home' : '/student/home';
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="text-center">
        <p className="text-8xl mb-4">🔍</p>
        <h1 className="text-4xl font-bold text-slate-900 mb-2">404</h1>
        <p className="text-slate-500 mb-6">Page not found</p>
        <Link to={home} className="btn-primary">Go Home</Link>
      </div>
    </div>
  );
}
