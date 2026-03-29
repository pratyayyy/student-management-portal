import { useState, useEffect, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { studentService, feeService } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';

export default function StudentDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const fileRef = useRef();

  const [student, setStudent] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [uploadingPic, setUploadingPic] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  useEffect(() => {
    Promise.all([
      studentService.getById(id),
      feeService.getByStudent(id),
    ])
      .then(([sRes, fRes]) => {
        setStudent(sRes.data);
        setForm(sRes.data);
        setTransactions(fRes.data || []);
      })
      .catch(() => navigate('/home'))
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await studentService.update(id, form);
      setStudent(form);
      setEditing(false);
      showToast('Student details updated successfully');
    } catch (e) {
      showToast(e.response?.data?.message || 'Update failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handlePicUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploadingPic(true);
    try {
      const res = await studentService.uploadPicture(id, file);
      setStudent(s => ({ ...s, profilePictureUrl: res.data.pictureUrl }));
      showToast('Profile picture updated');
    } catch {
      showToast('Upload failed', 'error');
    } finally {
      setUploadingPic(false);
    }
  };

  const handleDeletePic = async () => {
    try {
      await studentService.deletePicture(id);
      setStudent(s => ({ ...s, profilePictureUrl: null }));
      showToast('Profile picture removed');
    } catch {
      showToast('Delete failed', 'error');
    }
  };

  if (loading) return (
    <Layout>
      <div className="flex justify-center py-20">
        <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
      </div>
    </Layout>
  );

  const totalFees = transactions.reduce((sum, t) => sum + (t.amount || 0), 0);
  const isAdmin = user?.role === 'ADMIN';

  return (
    <Layout>
      {/* Toast */}
      {toast && (
        <div className={`fixed top-20 right-4 z-50 px-4 py-3 rounded-xl shadow-lg text-sm font-medium transition ${
          toast.type === 'error' ? 'bg-red-500 text-white' : 'bg-emerald-500 text-white'
        }`}>
          {toast.msg}
        </div>
      )}

      <div className="max-w-4xl mx-auto">
        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-slate-500 mb-6">
          <Link to="/home" className="hover:text-indigo-600">Home</Link>
          <span>/</span>
          <span className="text-slate-900 font-medium">{student?.name}</span>
        </div>

        {/* Profile card */}
        <div className="card mb-6">
          <div className="bg-gradient-to-r from-indigo-600 to-violet-600 px-6 py-6 rounded-t-xl">
            <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
              {/* Profile picture */}
              <div className="relative">
                {student?.profilePictureUrl ? (
                  <img
                    src={student.profilePictureUrl}
                    alt="Profile"
                    className="w-20 h-20 rounded-2xl border-2 border-white/40 object-cover"
                  />
                ) : (
                  <div className="w-20 h-20 rounded-2xl bg-white/20 border-2 border-white/40 flex items-center justify-center text-3xl text-white font-bold">
                    {student?.name?.[0]}
                  </div>
                )}
                {isAdmin && (
                  <div className="absolute -bottom-2 -right-2 flex gap-1">
                    <button
                      onClick={() => fileRef.current?.click()}
                      disabled={uploadingPic}
                      className="w-6 h-6 bg-white rounded-full shadow flex items-center justify-center text-xs hover:bg-indigo-50 transition"
                      title="Change picture"
                    >📷</button>
                    {student?.profilePictureUrl && (
                      <button
                        onClick={handleDeletePic}
                        className="w-6 h-6 bg-white rounded-full shadow flex items-center justify-center text-xs hover:bg-red-50 transition"
                        title="Remove picture"
                      >🗑️</button>
                    )}
                  </div>
                )}
                <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handlePicUpload} />
              </div>

              <div className="flex-1">
                <h1 className="text-2xl font-bold text-white">{student?.name}</h1>
                <p className="text-indigo-200">{student?.standard}</p>
                <div className="flex flex-wrap gap-2 mt-2">
                  <span className="px-2 py-0.5 bg-white/20 text-white text-xs rounded-full font-mono">{student?.studentId}</span>
                </div>
              </div>

              {isAdmin && (
                <div className="flex gap-2">
                  {editing ? (
                    <>
                      <button onClick={handleSave} disabled={saving} className="btn-primary text-xs py-1.5">
                        {saving ? 'Saving…' : '✓ Save'}
                      </button>
                      <button onClick={() => { setEditing(false); setForm(student); }} className="btn-secondary text-xs py-1.5">
                        Cancel
                      </button>
                    </>
                  ) : (
                    <button onClick={() => setEditing(true)} className="flex items-center gap-1.5 px-3 py-1.5 bg-white/20 text-white rounded-lg text-xs font-medium hover:bg-white/30 transition">
                      ✏️ Edit
                    </button>
                  )}
                  <Link to={`/accept/${id}`} className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-500 text-white rounded-lg text-xs font-medium hover:bg-emerald-600 transition">
                    💳 Add Fee
                  </Link>
                </div>
              )}
            </div>
          </div>

          {/* Details grid */}
          <div className="p-6">
            {editing ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {[
                  { key: 'name', label: 'Full Name' },
                  { key: 'standard', label: 'Course / Class' },
                  { key: 'phoneNumber', label: 'Phone Number' },
                  { key: 'alternateNumber', label: 'Alternate Number' },
                  { key: 'guardiansName', label: "Guardian's Name" },
                  { key: 'address', label: 'Address', span: true },
                ].map(({ key, label, span }) => (
                  <div key={key} className={span ? 'sm:col-span-2' : ''}>
                    <label className="label">{label}</label>
                    <input
                      type="text"
                      value={form[key] || ''}
                      onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
                      className="input"
                    />
                  </div>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                {[
                  { label: 'Phone', value: student?.phoneNumber },
                  { label: 'Alternate Number', value: student?.alternateNumber },
                  { label: "Guardian's Name", value: student?.guardiansName },
                  { label: 'Address', value: student?.address },
                ].map(({ label, value }) => (
                  <div key={label}>
                    <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{label}</p>
                    <p className="text-slate-900 mt-0.5">{value || '—'}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Transactions */}
        <div className="card">
          <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <h2 className="font-semibold text-slate-900">Fee History</h2>
            <span className="text-sm text-emerald-600 font-semibold">
              Total: ₹{totalFees.toLocaleString()}
            </span>
          </div>
          {transactions.length === 0 ? (
            <div className="py-12 text-center text-slate-400">
              <p className="text-3xl mb-2">💳</p>
              <p>No transactions yet</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider">
                  <tr>
                    <th className="text-left px-6 py-3">Bill No.</th>
                    <th className="text-left px-6 py-3">For Month</th>
                    <th className="text-left px-6 py-3">Amount</th>
                    <th className="text-left px-6 py-3">Payment Date</th>
                    <th className="text-left px-6 py-3">Recorded</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {transactions.map(t => (
                    <tr key={t.id} className="hover:bg-slate-50/60">
                      <td className="px-6 py-3 font-mono text-xs text-slate-600">{t.billNumber}</td>
                      <td className="px-6 py-3 text-slate-700">{t.paymentForMonth}</td>
                      <td className="px-6 py-3 font-semibold text-emerald-600">₹{t.amount}</td>
                      <td className="px-6 py-3 text-slate-500">{t.paymentReceivedDate}</td>
                      <td className="px-6 py-3 text-slate-400 text-xs">{t.transactionDate?.slice(0, 10)}</td>
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
