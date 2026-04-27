import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { feeService, studentService } from '../services/api';
import Layout from '../components/Layout';

export default function AcceptFee() {
  const { studentId } = useParams();
  const navigate = useNavigate();
  const [student, setStudent] = useState(null);
  const [form, setForm] = useState({
    studentId, billNumber: '', amount: '', paymentReceivedDate: '', paymentForMonth: ''
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    studentService.getById(studentId).then(r => setStudent(r.data)).catch(console.error);
  }, [studentId]);

  const set = (k) => (e) => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await feeService.create({ ...form, amount: Number(form.amount) });
      navigate(`/students/${studentId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record payment');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout>
      <div className="max-w-lg mx-auto">
        <div className="flex items-center gap-2 text-sm text-slate-500 mb-6">
          <Link to="/home" className="hover:text-indigo-600">Home</Link>
          <span>/</span>
          {student && <Link to={`/students/${studentId}`} className="hover:text-indigo-600">{student.name}</Link>}
          <span>/</span>
          <span className="text-slate-900 font-medium">Accept Fee</span>
        </div>

        <div className="card overflow-hidden">
          <div className="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-5">
            <h1 className="text-white font-bold text-xl">💳 Record Fee Payment</h1>
            {student && (
              <p className="text-emerald-100 text-sm mt-1">
                For: {student.name} ({student.studentId})
              </p>
            )}
          </div>

          {error && (
            <div className="mx-6 mt-4 px-4 py-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
              ⚠️ {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            <div>
              <label className="label">Bill Number *</label>
              <input type="text" required value={form.billNumber} onChange={set('billNumber')} placeholder="e.g. BILL-2024-001" className="input" />
            </div>
            <div>
              <label className="label">Amount (₹) *</label>
              <input type="number" required min="1" value={form.amount} onChange={set('amount')} placeholder="Enter amount in rupees" className="input" />
            </div>
            <div>
              <label className="label">Payment Date *</label>
              <input type="date" required value={form.paymentReceivedDate} onChange={set('paymentReceivedDate')} className="input" />
            </div>
            <div>
              <label className="label">Payment For Month *</label>
              <input type="text" required value={form.paymentForMonth} onChange={set('paymentForMonth')} placeholder="e.g. January 2024" className="input" />
            </div>

            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => navigate(-1)} className="btn-secondary flex-1">Cancel</button>
              <button type="submit" disabled={saving} className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-2 bg-emerald-600 text-white rounded-lg font-medium text-sm hover:bg-emerald-700 transition disabled:opacity-50">
                {saving ? 'Recording…' : '✓ Record Payment'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
}
