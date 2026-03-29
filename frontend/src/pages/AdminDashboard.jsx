import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminService, studentService } from '../services/api';
import Layout from '../components/Layout';

function StatCard({ label, value, icon, color, sub }) {
  return (
    <div className="card p-6">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="text-3xl font-bold text-slate-900 mt-1">{value ?? '—'}</p>
          {sub && <p className="text-xs text-slate-400 mt-1">{sub}</p>}
        </div>
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-2xl ${color}`}>
          {icon}
        </div>
      </div>
    </div>
  );
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [recentStudents, setRecentStudents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      adminService.stats(),
      studentService.paginated(0, 5),
    ])
      .then(([statsRes, studentsRes]) => {
        setStats(statsRes.data);
        setRecentStudents(studentsRes.data.content || []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <Layout>
      <div className="flex justify-center py-20">
        <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
      </div>
    </Layout>
  );

  return (
    <Layout>
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-slate-900">Admin Dashboard</h1>
          <p className="text-slate-500 mt-1">Welcome back! Here&apos;s what&apos;s happening today.</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          <StatCard label="Total Students" value={stats?.totalStudents} icon="🎓" color="bg-indigo-50" sub="All enrolled students" />
          <StatCard label="Registered Users" value={stats?.registeredUsers} icon="👤" color="bg-violet-50" sub="Active accounts" />
          <StatCard label="Student Accounts" value={stats?.studentAccounts} icon="📱" color="bg-blue-50" sub="Student logins" />
        </div>

        {/* Quick actions */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
          <Link to="/add" className="card p-5 flex items-center gap-4 hover:border-indigo-200 hover:shadow-md transition group">
            <div className="w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center text-xl group-hover:bg-indigo-500 transition">
              <span className="group-hover:text-white">➕</span>
            </div>
            <div>
              <p className="font-semibold text-slate-900">Add Student</p>
              <p className="text-xs text-slate-500">Register manually</p>
            </div>
          </Link>
          <Link to="/bulk-import" className="card p-5 flex items-center gap-4 hover:border-indigo-200 hover:shadow-md transition group">
            <div className="w-10 h-10 bg-emerald-100 rounded-xl flex items-center justify-center text-xl group-hover:bg-emerald-500 transition">
              <span className="group-hover:text-white">📊</span>
            </div>
            <div>
              <p className="font-semibold text-slate-900">Bulk Import</p>
              <p className="text-xs text-slate-500">Upload Excel file</p>
            </div>
          </Link>
          <Link to="/admin/students" className="card p-5 flex items-center gap-4 hover:border-indigo-200 hover:shadow-md transition group">
            <div className="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center text-xl group-hover:bg-purple-500 transition">
              <span className="group-hover:text-white">📋</span>
            </div>
            <div>
              <p className="font-semibold text-slate-900">All Students</p>
              <p className="text-xs text-slate-500">View &amp; manage</p>
            </div>
          </Link>
        </div>

        {/* Recent students table */}
        <div className="card">
          <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <h2 className="font-semibold text-slate-900">Recent Students</h2>
            <Link to="/admin/students" className="text-sm text-indigo-600 hover:underline">View all →</Link>
          </div>
          {recentStudents.length === 0 ? (
            <div className="py-12 text-center text-slate-400">
              <p className="text-4xl mb-3">📭</p>
              <p>No students registered yet</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider">
                  <tr>
                    <th className="text-left px-6 py-3">Student ID</th>
                    <th className="text-left px-6 py-3">Name</th>
                    <th className="text-left px-6 py-3">Course</th>
                    <th className="text-left px-6 py-3">Contact</th>
                    <th className="text-left px-6 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {recentStudents.map(s => (
                    <tr key={s.studentId} className="hover:bg-slate-50/50 transition">
                      <td className="px-6 py-3">
                        <span className="px-2 py-1 bg-indigo-50 text-indigo-700 rounded text-xs font-mono">{s.studentId}</span>
                      </td>
                      <td className="px-6 py-3 font-medium text-slate-900">{s.name}</td>
                      <td className="px-6 py-3 text-slate-500">{s.standard}</td>
                      <td className="px-6 py-3 text-slate-500">{s.phoneNumber}</td>
                      <td className="px-6 py-3">
                        <div className="flex gap-2">
                          <Link to={`/students/${s.studentId}`} className="text-indigo-600 hover:text-indigo-800 font-medium text-xs">View</Link>
                          <Link to={`/accept/${s.studentId}`} className="text-emerald-600 hover:text-emerald-800 font-medium text-xs">Fee</Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
