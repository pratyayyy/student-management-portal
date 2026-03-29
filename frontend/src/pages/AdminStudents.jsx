import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { studentService } from '../services/api';
import Layout from '../components/Layout';

export default function AdminStudents() {
  const [students, setStudents] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [loading, setLoading] = useState(true);

  // Debounce search
  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(query), 300);
    return () => clearTimeout(t);
  }, [query]);

  const fetchStudents = useCallback(async () => {
    setLoading(true);
    try {
      const res = await studentService.search(debouncedQuery, page, pageSize);
      setStudents(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalRecords(res.data.totalElements || 0);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, [debouncedQuery, page, pageSize]);

  useEffect(() => { fetchStudents(); }, [fetchStudents]);

  return (
    <Layout>
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">All Students</h1>
            <p className="text-slate-500 text-sm mt-1">{totalRecords} students total</p>
          </div>
          <div className="flex gap-2">
            <Link to="/add" className="btn-primary">➕ Add Student</Link>
            <Link to="/bulk-import" className="btn-secondary">📊 Bulk Import</Link>
          </div>
        </div>

        <div className="card">
          {/* Search & controls */}
          <div className="flex flex-col sm:flex-row gap-3 p-4 border-b border-slate-100">
            <div className="flex-1 relative">
              <svg className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                value={query}
                onChange={e => { setQuery(e.target.value); setPage(0); }}
                placeholder="Search by name or ID…"
                className="input pl-9"
              />
            </div>
            <select
              value={pageSize}
              onChange={e => { setPageSize(Number(e.target.value)); setPage(0); }}
              className="input w-auto"
            >
              <option value={10}>10 / page</option>
              <option value={20}>20 / page</option>
              <option value={30}>30 / page</option>
            </select>
          </div>

          {loading ? (
            <div className="flex justify-center py-16">
              <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : students.length === 0 ? (
            <div className="py-16 text-center text-slate-400">
              <p className="text-4xl mb-3">🔍</p>
              <p>No students found</p>
              {query && <p className="text-sm mt-1">Try a different search term</p>}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider">
                  <tr>
                    <th className="text-left px-6 py-3">Student ID</th>
                    <th className="text-left px-6 py-3">Name</th>
                    <th className="text-left px-6 py-3">Course/Class</th>
                    <th className="text-left px-6 py-3">Contact</th>
                    <th className="text-left px-6 py-3">Guardian</th>
                    <th className="text-left px-6 py-3">Address</th>
                    <th className="text-left px-6 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {students.map(s => (
                    <tr key={s.studentId} className="hover:bg-slate-50/60 transition">
                      <td className="px-6 py-3">
                        <span className="px-2 py-1 bg-indigo-50 text-indigo-700 rounded text-xs font-mono">{s.studentId}</span>
                      </td>
                      <td className="px-6 py-3 font-medium text-slate-900">{s.name}</td>
                      <td className="px-6 py-3 text-slate-500">{s.standard}</td>
                      <td className="px-6 py-3 text-slate-500">{s.phoneNumber}</td>
                      <td className="px-6 py-3 text-slate-500">{s.guardiansName}</td>
                      <td className="px-6 py-3 text-slate-400 max-w-xs truncate">{s.address}</td>
                      <td className="px-6 py-3">
                        <div className="flex gap-3">
                          <Link to={`/students/${s.studentId}`} className="text-indigo-600 hover:text-indigo-800 font-medium">View</Link>
                          <Link to={`/accept/${s.studentId}`} className="text-emerald-600 hover:text-emerald-800 font-medium">Fee</Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between px-6 py-3 border-t border-slate-100">
              <p className="text-sm text-slate-500">
                Page {page + 1} of {totalPages}
              </p>
              <div className="flex gap-1">
                <button
                  onClick={() => setPage(0)}
                  disabled={page === 0}
                  className="px-2 py-1 text-xs rounded border border-slate-200 disabled:opacity-40 hover:bg-slate-50"
                >«</button>
                <button
                  onClick={() => setPage(p => p - 1)}
                  disabled={page === 0}
                  className="px-3 py-1 text-xs rounded border border-slate-200 disabled:opacity-40 hover:bg-slate-50"
                >Prev</button>
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  const p = Math.max(0, Math.min(page - 2, totalPages - 5)) + i;
                  return (
                    <button
                      key={p}
                      onClick={() => setPage(p)}
                      className={`px-3 py-1 text-xs rounded border ${
                        p === page
                          ? 'bg-indigo-600 text-white border-indigo-600'
                          : 'border-slate-200 hover:bg-slate-50'
                      }`}
                    >{p + 1}</button>
                  );
                })}
                <button
                  onClick={() => setPage(p => p + 1)}
                  disabled={page >= totalPages - 1}
                  className="px-3 py-1 text-xs rounded border border-slate-200 disabled:opacity-40 hover:bg-slate-50"
                >Next</button>
                <button
                  onClick={() => setPage(totalPages - 1)}
                  disabled={page >= totalPages - 1}
                  className="px-2 py-1 text-xs rounded border border-slate-200 disabled:opacity-40 hover:bg-slate-50"
                >»</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
