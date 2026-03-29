import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { studentService } from '../services/api';
import Layout from '../components/Layout';

export default function AddStudent() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    studentId: '', name: '', phoneNumber: '', alternateNumber: '',
    standard: '', address: '', guardiansName: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const set = (k) => (e) => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await studentService.create(form);
      navigate('/home');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add student');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-slate-900">Add New Student</h1>
          <p className="text-slate-500 mt-1">Register a new student to the portal</p>
        </div>

        <div className="card overflow-hidden">
          <div className="bg-gradient-to-r from-indigo-600 to-violet-600 px-6 py-4">
            <h2 className="text-white font-semibold">Student Information</h2>
            <p className="text-indigo-200 text-sm">Fill in all required fields</p>
          </div>

          {error && (
            <div className="mx-6 mt-4 px-4 py-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
              ⚠️ {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-5">
            <div>
              <label className="label">Student ID <span className="text-slate-400 font-normal">(auto if blank)</span></label>
              <input type="text" value={form.studentId} onChange={set('studentId')} placeholder="e.g. 2024-0001" className="input" />
            </div>
            <div>
              <label className="label">Full Name *</label>
              <input type="text" required value={form.name} onChange={set('name')} placeholder="Student's full name" className="input" />
            </div>
            <div>
              <label className="label">Phone Number *</label>
              <input type="tel" required value={form.phoneNumber} onChange={set('phoneNumber')} placeholder="10-digit mobile number" className="input" />
            </div>
            <div>
              <label className="label">Alternate Number</label>
              <input type="tel" value={form.alternateNumber} onChange={set('alternateNumber')} placeholder="Optional" className="input" />
            </div>
            <div>
              <label className="label">Course / Class *</label>
              <input type="text" required value={form.standard} onChange={set('standard')} placeholder="e.g. Grade 10, B.Sc" className="input" />
            </div>
            <div>
              <label className="label">Guardian&apos;s Name *</label>
              <input type="text" required value={form.guardiansName} onChange={set('guardiansName')} placeholder="Parent or guardian" className="input" />
            </div>
            <div className="sm:col-span-2">
              <label className="label">Address</label>
              <textarea value={form.address} onChange={set('address')} placeholder="Full residential address" rows={3} className="input resize-none" />
            </div>

            <div className="sm:col-span-2 flex gap-3 justify-end pt-2">
              <button type="button" onClick={() => navigate(-1)} className="btn-secondary">Cancel</button>
              <button type="submit" disabled={saving} className="btn-primary">
                {saving ? 'Adding…' : '➕ Add Student'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
}
