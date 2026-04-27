/**
 * PreviewPanel – live preview of the promotional website configuration.
 * Renders a simplified mock of how the promo site sections would look
 * based on the current featureConfig and contentConfig.
 */
import useAdminConfigStore from '../../store/adminConfigStore';

const SECTION_ORDER = [
  { key: 'hero',         label: 'Hero Section',       color: 'bg-indigo-600 text-white' },
  { key: 'carousel',     label: 'Carousel',           color: 'bg-slate-700 text-white' },
  { key: 'about',        label: 'About Section',      color: 'bg-blue-50 text-blue-800' },
  { key: 'whyChooseUs',  label: 'Why Choose Us',      color: 'bg-emerald-50 text-emerald-800' },
  { key: 'courses',      label: 'Courses',            color: 'bg-violet-50 text-violet-800' },
  { key: 'results',      label: 'Results',            color: 'bg-amber-50 text-amber-800' },
  { key: 'faculty',      label: 'Faculty',            color: 'bg-pink-50 text-pink-800' },
  { key: 'testimonials', label: 'Testimonials',       color: 'bg-sky-50 text-sky-800' },
  { key: 'blog',         label: 'Blog',               color: 'bg-orange-50 text-orange-800' },
  { key: 'resources',    label: 'Resources',          color: 'bg-teal-50 text-teal-800' },
  { key: 'demoBooking',  label: 'Demo Booking',       color: 'bg-lime-50 text-lime-800' },
  { key: 'scholarship',  label: 'Scholarship',        color: 'bg-cyan-50 text-cyan-800' },
  { key: 'faq',          label: 'FAQ',                color: 'bg-slate-50 text-slate-800' },
  { key: 'contact',      label: 'Contact',            color: 'bg-gray-700 text-white' },
];

function HeroPreview({ content }) {
  return (
    <div className="p-6 bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-lg">
      <h2 className="text-lg font-bold mb-1">{content['hero.title'] || 'Hero Title'}</h2>
      <p className="text-sm opacity-90 mb-3">{content['hero.subtitle'] || 'Subheading text…'}</p>
      <button className="px-4 py-1.5 bg-white text-indigo-700 rounded-full text-sm font-semibold">
        {content['hero.cta'] || 'Enquire Now'}
      </button>
    </div>
  );
}

function AboutPreview({ content }) {
  return (
    <div className="p-4 bg-blue-50 rounded-lg">
      <h3 className="font-semibold text-blue-900 mb-1">{content['about.heading'] || 'About Us'}</h3>
      <p className="text-xs text-blue-700 line-clamp-2">{content['about.para1'] || 'About paragraph 1…'}</p>
    </div>
  );
}

function ContactPreview({ content }) {
  return (
    <div className="p-4 bg-gray-700 text-white rounded-lg">
      <h3 className="font-semibold mb-2">Contact Us</h3>
      <div className="space-y-1 text-xs opacity-90">
        <p>📞 {content['contact.phone'] || '+91 00000 00000'}</p>
        <p>✉️ {content['contact.email'] || 'info@ija.edu'}</p>
        <p>🕐 {content['contact.hours'] || 'Mon–Sat: 9 AM – 6 PM'}</p>
      </div>
    </div>
  );
}

function GenericSectionPreview({ label, color }) {
  return (
    <div className={`p-3 rounded-lg text-sm font-medium flex items-center gap-2 ${color}`}>
      <span className="w-2 h-2 rounded-full bg-current opacity-50" />
      {label}
    </div>
  );
}

export default function PreviewPanel() {
  const { featureConfig, contentConfig } = useAdminConfigStore();

  const enabledSections = SECTION_ORDER.filter((s) => featureConfig[s.key] !== false);

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-slate-800">Promotional Website Preview</h3>
          <p className="text-xs text-slate-500 mt-0.5">
            {enabledSections.length} of {SECTION_ORDER.length} sections visible
          </p>
        </div>
        <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-emerald-50 text-emerald-700 text-xs font-medium rounded-full border border-emerald-200">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
          Live
        </span>
      </div>

      {/* Mock browser chrome */}
      <div className="border border-slate-200 rounded-xl overflow-hidden shadow-sm">
        {/* Browser bar */}
        <div className="flex items-center gap-2 px-3 py-2 bg-slate-100 border-b border-slate-200">
          <div className="flex gap-1.5">
            <div className="w-3 h-3 rounded-full bg-red-400" />
            <div className="w-3 h-3 rounded-full bg-yellow-400" />
            <div className="w-3 h-3 rounded-full bg-green-400" />
          </div>
          <div className="flex-1 bg-white rounded-md px-3 py-1 text-xs text-slate-400 font-mono">
            https://your-institute.com
          </div>
        </div>

        {/* Simulated page content */}
        <div className="bg-white p-3 space-y-2 max-h-[600px] overflow-y-auto">
          {/* Nav */}
          <div className="flex items-center justify-between py-2 px-3 bg-indigo-600 text-white rounded-lg text-xs">
            <span className="font-bold">IJA</span>
            <div className="flex gap-3 opacity-90">
              <span>About</span><span>Courses</span><span>Contact</span>
              {featureConfig.login !== false && <span className="bg-white text-indigo-700 px-2 py-0.5 rounded-full font-semibold">Login</span>}
            </div>
          </div>

          {/* WhatsApp button */}
          {featureConfig.whatsapp !== false && (
            <div className="fixed-mock flex justify-end">
              <div className="inline-flex items-center gap-1 px-2 py-1 bg-green-500 text-white rounded-full text-xs">
                💚 WhatsApp
              </div>
            </div>
          )}

          {/* Enabled sections */}
          {enabledSections.map((section) => {
            if (section.key === 'hero') return <HeroPreview key={section.key} content={contentConfig} />;
            if (section.key === 'about') return <AboutPreview key={section.key} content={contentConfig} />;
            if (section.key === 'contact') return <ContactPreview key={section.key} content={contentConfig} />;
            return <GenericSectionPreview key={section.key} label={section.label} color={section.color} />;
          })}

          {enabledSections.length === 0 && (
            <div className="py-12 text-center text-slate-400">
              <p className="text-3xl mb-2">🙈</p>
              <p className="text-sm">All sections are hidden. Enable some sections in the Features tab.</p>
            </div>
          )}
        </div>
      </div>

      <p className="text-xs text-slate-400 text-center">
        This is a simplified preview. The actual promotional website may render differently.
      </p>
    </div>
  );
}
