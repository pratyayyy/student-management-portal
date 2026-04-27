/**
 * FeatureToggles – toggle switches for each promotional website section.
 * Changes are persisted to the backend in real time via the Zustand store.
 */
import useAdminConfigStore from '../../store/adminConfigStore';

const FEATURE_LABELS = {
  hero:         { label: 'Hero Section',       icon: '🌟', description: 'Main banner with headline and CTA' },
  carousel:     { label: 'Carousel',           icon: '🖼️', description: 'Image slideshow carousel' },
  about:        { label: 'About Section',      icon: '📖', description: 'Institute background and mission' },
  whyChooseUs:  { label: 'Why Choose Us',      icon: '✨', description: 'Key differentiators and benefits' },
  courses:      { label: 'Courses',            icon: '📚', description: 'List of courses offered' },
  results:      { label: 'Results',            icon: '🏆', description: 'Student achievements and results' },
  faculty:      { label: 'Faculty',            icon: '👨‍🏫', description: 'Faculty profiles' },
  testimonials: { label: 'Testimonials',       icon: '💬', description: 'Student reviews and feedback' },
  blog:         { label: 'Blog',               icon: '✍️', description: 'Blog posts and articles' },
  resources:    { label: 'Resources',          icon: '📄', description: 'Downloadable study materials' },
  demoBooking:  { label: 'Demo Booking',       icon: '📅', description: 'Free demo class booking form' },
  scholarship:  { label: 'Scholarship',        icon: '🎓', description: 'Scholarship information section' },
  faq:          { label: 'FAQ',                icon: '❓', description: 'Frequently asked questions' },
  contact:      { label: 'Contact',            icon: '📞', description: 'Contact details and map' },
  whatsapp:     { label: 'WhatsApp Button',    icon: '💚', description: 'Floating WhatsApp CTA button' },
  leadForms:    { label: 'Lead Forms',         icon: '📝', description: 'Enquiry / lead capture forms' },
  login:        { label: 'Student Login Link', icon: '🔐', description: 'Login link in navigation' },
};

function ToggleSwitch({ enabled, onChange }) {
  return (
    <button
      role="switch"
      aria-checked={enabled}
      onClick={() => onChange(!enabled)}
      className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-1 ${
        enabled ? 'bg-indigo-600' : 'bg-slate-200'
      }`}
    >
      <span
        className={`inline-block h-4 w-4 transform rounded-full bg-white shadow-sm transition-transform ${
          enabled ? 'translate-x-6' : 'translate-x-1'
        }`}
      />
    </button>
  );
}

export default function FeatureToggles() {
  const { featureConfig, setFeatureToggle } = useAdminConfigStore();

  const enabledCount = Object.values(featureConfig).filter(Boolean).length;
  const totalCount = Object.keys(FEATURE_LABELS).length;

  return (
    <div className="space-y-4">
      {/* Summary bar */}
      <div className="flex items-center gap-4 p-4 bg-indigo-50 rounded-xl border border-indigo-100">
        <div className="w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center text-xl">⚙️</div>
        <div>
          <p className="text-sm font-semibold text-indigo-900">{enabledCount} of {totalCount} sections enabled</p>
          <p className="text-xs text-indigo-600">Changes take effect instantly on the promotional website.</p>
        </div>
      </div>

      {/* Toggle list */}
      <div className="space-y-2">
        {Object.entries(FEATURE_LABELS).map(([key, { label, icon, description }]) => {
          const enabled = featureConfig[key] !== false;
          return (
            <div
              key={key}
              className={`flex items-center gap-4 p-4 rounded-xl border transition ${
                enabled
                  ? 'bg-white border-slate-200 hover:border-indigo-200'
                  : 'bg-slate-50 border-slate-100 opacity-60'
              }`}
            >
              <span className="text-xl w-8 text-center shrink-0">{icon}</span>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-slate-800">{label}</p>
                <p className="text-xs text-slate-500 truncate">{description}</p>
              </div>
              <ToggleSwitch enabled={enabled} onChange={(value) => setFeatureToggle(key, value)} />
            </div>
          );
        })}
      </div>
    </div>
  );
}
