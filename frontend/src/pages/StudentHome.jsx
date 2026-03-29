import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { studentService, feeService } from '../services/api';
import Layout from '../components/Layout';

export default function StudentHome() {
  const { user } = useAuth();
  const [student, setStudent] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user?.studentId) { setLoading(false); return; }
    Promise.all([
      studentService.getById(user.studentId),
      feeService.getByStudent(user.studentId),
    ])
      .then(([sRes, fRes]) => {
        setStudent(sRes.data);
        setTransactions(fRes.data || []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [user]);

  if (loading) return (
    <Layout>
      <div className="flex justify-center py-20">
        <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
      </div>
    </Layout>
  );

  const totalFees = transactions.reduce((sum, t) => sum + (t.amount || 0), 0);

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-slate-900">My Dashboard</h1>
          <p className="text-slate-500 mt-1">Welcome to Institute of Junior Accountants, {user?.username}!</p>
        </div>

        {/* Profile card */}
        {student ? (
          <div className="card mb-6">
            <div className="bg-gradient-to-r from-indigo-600 to-violet-600 px-6 py-5 rounded-t-xl">
              <div className="flex items-center gap-4">
                {student.profilePictureUrl ? (
                  <img
                    src={student.profilePictureUrl}
                    alt="Profile"
                    className="w-16 h-16 rounded-full border-2 border-white/50 object-cover"
                  />
                ) : (
                  <div className="w-16 h-16 rounded-full bg-white/20 border-2 border-white/50 flex items-center justify-center text-2xl text-white font-bold">
                    {student.name?.[0]}
                  </div>
                )}
                <div>
                  <h2 className="text-xl font-bold text-white">{student.name}</h2>
                  <p className="text-indigo-200 text-sm">{student.standard}</p>
                  <span className="inline-block mt-1 px-2 py-0.5 bg-white/20 text-white text-xs rounded-full font-mono">
                    {student.studentId}
                  </span>
                </div>
              </div>
            </div>
            <div className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
              {[
                { label: 'Phone', value: student.phoneNumber },
                { label: 'Alternate', value: student.alternateNumber },
                { label: 'Guardian', value: student.guardiansName },
                { label: 'Address', value: student.address },
              ].map(({ label, value }) => value ? (
                <div key={label}>
                  <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">{label}</p>
                  <p className="text-slate-900 font-medium mt-0.5">{value}</p>
                </div>
              ) : null)}
            </div>
          </div>
        ) : (
          <div className="card p-8 text-center text-slate-400 mb-6">
            <p className="text-3xl mb-2">👤</p>
            <p>No student profile linked to this account</p>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="card p-5">
            <p className="text-sm text-slate-500">Total Payments</p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{transactions.length}</p>
          </div>
          <div className="card p-5">
            <p className="text-sm text-slate-500">Total Fees Paid</p>
            <p className="text-2xl font-bold text-emerald-600 mt-1">₹{totalFees.toLocaleString()}</p>
          </div>
        </div>

        {/* Transaction history */}
        <div className="card">
          <div className="px-6 py-4 border-b border-slate-100">
            <h2 className="font-semibold text-slate-900">Fee Payment History</h2>
          </div>
          {transactions.length === 0 ? (
            <div className="py-12 text-center text-slate-400">
              <p className="text-3xl mb-2">💳</p>
              <p>No transactions recorded</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider">
                  <tr>
                    <th className="text-left px-6 py-3">Bill No.</th>
                    <th className="text-left px-6 py-3">Month</th>
                    <th className="text-left px-6 py-3">Amount</th>
                    <th className="text-left px-6 py-3">Payment Date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {transactions.map(t => (
                    <tr key={t.id} className="hover:bg-slate-50/60">
                      <td className="px-6 py-3 font-mono text-xs text-slate-600">{t.billNumber}</td>
                      <td className="px-6 py-3 text-slate-700">{t.paymentForMonth}</td>
                      <td className="px-6 py-3 font-semibold text-emerald-600">₹{t.amount}</td>
                      <td className="px-6 py-3 text-slate-500">{t.paymentReceivedDate}</td>
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
