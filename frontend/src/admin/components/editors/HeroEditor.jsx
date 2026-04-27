/** HeroEditor – edit hero section text (title, subtitle, CTA). */
import { useState } from 'react';
import useAdminConfigStore from '../../../store/adminConfigStore';

export default function HeroEditor() {
  const { contentConfig, setContentSection, saving } = useAdminConfigStore();
  const [local, setLocal] = useState({
    'hero.title': contentConfig['hero.title'] || '',
    'hero.subtitle': contentConfig['hero.subtitle'] || '',
    'hero.cta': contentConfig['hero.cta'] || '',
  });
  const [success, setSuccess] = useState(false);

  const handleChange = (key, value) => setLocal((prev) => ({ ...prev, [key]: value }));

  const handleSave = async () => {
    await setContentSection(local);
    setSuccess(true);
    setTimeout(() => setSuccess(false), 3000);
  };

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-slate-700 mb-1">Headline</label>
        <input
          className="input w-full"
          value={local['hero.title']}
          onChange={(e) => handleChange('hero.title', e.target.value)}
          placeholder="Enter headline text"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700 mb-1">Subheading</label>
        <textarea
          rows={3}
          className="input w-full resize-y"
          value={local['hero.subtitle']}
          onChange={(e) => handleChange('hero.subtitle', e.target.value)}
          placeholder="Enter subheading text"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700 mb-1">CTA Button Text</label>
        <input
          className="input w-full"
          value={local['hero.cta']}
          onChange={(e) => handleChange('hero.cta', e.target.value)}
          placeholder="e.g. Enquire Now"
        />
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
