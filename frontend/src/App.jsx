import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import AdminDashboard from './pages/AdminDashboard';
import AdminStudents from './pages/AdminStudents';
import StudentHome from './pages/StudentHome';
import StudentDetails from './pages/StudentDetails';
import AddStudent from './pages/AddStudent';
import AcceptFee from './pages/AcceptFee';
import BulkImport from './pages/BulkImport';
import NotFound from './pages/NotFound';

function RootRedirect() {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/home' : '/student/home'} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/login" element={<Login />} />

          {/* Admin routes */}
          <Route path="/home" element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/home" element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/students" element={<ProtectedRoute role="ADMIN"><AdminStudents /></ProtectedRoute>} />
          <Route path="/add" element={<ProtectedRoute role="ADMIN"><AddStudent /></ProtectedRoute>} />
          <Route path="/bulk-import" element={<ProtectedRoute role="ADMIN"><BulkImport /></ProtectedRoute>} />
          <Route path="/accept/:studentId" element={<ProtectedRoute role="ADMIN"><AcceptFee /></ProtectedRoute>} />
          <Route path="/students/:id" element={<ProtectedRoute><StudentDetails /></ProtectedRoute>} />

          {/* Student routes */}
          <Route path="/student/home" element={<ProtectedRoute role="STUDENT"><StudentHome /></ProtectedRoute>} />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

