/** ContactEditor – edit contact details shown on the promotional website. */
import { useState } from 'react';
import useAdminConfigStore from '../../../store/adminConfigStore';

export default function ContactEditor() {
  const { contentConfig, setContentSection, saving } = useAdminConfigStore();
  const [local, setLocal] = useState({
    'contact.phone': contentConfig['contact.phone'] || '',
    'contact.email': contentConfig['contact.email'] || '',
    'contact.address': contentConfig['contact.address'] || '',
    'contact.hours': contentConfig['contact.hours'] || '',
    'stats.students': contentConfig['stats.students'] || '',
    'stats.experience': contentConfig['stats.experience'] || '',
    'stats.placement': contentConfig['stats.placement'] || '',
  });
  const [success, setSuccess] = useState(false);

  const handleChange = (key, value) => setLocal((prev) => ({ ...prev, [key]: value }));

  const handleSave = async () => {
    await setContentSection(local);
    setSuccess(true);
    setTimeout(() => setSuccess(false), 3000);
  };

  const fields = [
    { key: 'contact.phone', label: 'Phone Number', multiline: false },
    { key: 'contact.email', label: 'Email Address', multiline: false },
    { key: 'contact.address', label: 'Office Address', multiline: true },
    { key: 'contact.hours', label: 'Office Hours', multiline: false },
  ];

  const statFields = [
    { key: 'stats.students', label: 'Students Enrolled (e.g. 2000+)' },
    { key: 'stats.experience', label: 'Years of Experience (e.g. 20+)' },
    { key: 'stats.placement', label: 'Placement Rate (e.g. 95%)' },
  ];

  return (
    <div className="space-y-6">
      <div className="space-y-4">
        <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">Contact Details</h3>
        {fields.map(({ key, label, multiline }) => (
          <div key={key}>
            <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
            {multiline ? (
              <textarea
                rows={2}
                className="input w-full resize-y"
                value={local[key]}
                onChange={(e) => handleChange(key, e.target.value)}
              />
            ) : (
              <input
                className="input w-full"
                value={local[key]}
                onChange={(e) => handleChange(key, e.target.value)}
              />
            )}
          </div>
        ))}
      </div>

      <div className="space-y-4">
        <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">Statistics / Highlights</h3>
        {statFields.map(({ key, label }) => (
          <div key={key}>
            <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
            <input
              className="input w-full"
              value={local[key]}
              onChange={(e) => handleChange(key, e.target.value)}
            />
          </div>
        ))}
      </div>

      <div className="flex items-center gap-3">
        <button onClick={handleSave} disabled={saving} className="btn-primary px-5 py-2 text-sm">
          {saving ? 'Saving…' : 'Save'}
        </button>
        {success && <span className="text-sm text-emerald-600 font-medium">✅ Saved!</span>}
      </div>
    </div>
  );
}
