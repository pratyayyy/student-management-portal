/**
 * ContentEditor – tabbed panel for editing all content sections.
 * Each tab renders the relevant section editor component.
 */
import { useState } from 'react';
import HeroEditor from './editors/HeroEditor';
import AboutEditor from './editors/AboutEditor';
import ContactEditor from './editors/ContactEditor';
import CoursesEditor from './editors/CoursesEditor';
import FacultyEditor from './editors/FacultyEditor';
import TestimonialsEditor from './editors/TestimonialsEditor';
import BlogEditor from './editors/BlogEditor';
import ResultsEditor from './editors/ResultsEditor';
import FAQEditor from './editors/FAQEditor';

const SECTIONS = [
  { id: 'hero',         label: 'Hero',         icon: '🌟', component: HeroEditor },
  { id: 'about',        label: 'About',        icon: '📖', component: AboutEditor },
  { id: 'contact',      label: 'Contact',      icon: '📞', component: ContactEditor },
  { id: 'courses',      label: 'Courses',      icon: '📚', component: CoursesEditor },
  { id: 'faculty',      label: 'Faculty',      icon: '👨‍🏫', component: FacultyEditor },
  { id: 'testimonials', label: 'Testimonials', icon: '💬', component: TestimonialsEditor },
  { id: 'blog',         label: 'Blog',         icon: '✍️', component: BlogEditor },
  { id: 'results',      label: 'Results',      icon: '🏆', component: ResultsEditor },
  { id: 'faq',          label: 'FAQ',          icon: '❓', component: FAQEditor },
];

export default function ContentEditor() {
  const [activeSection, setActiveSection] = useState('hero');
  const active = SECTIONS.find((s) => s.id === activeSection);
  const ActiveComponent = active?.component;

  return (
    <div className="flex gap-4 min-h-[500px]">
      {/* Section sidebar */}
      <div className="w-44 shrink-0 space-y-1">
        {SECTIONS.map((section) => (
          <button
            key={section.id}
            onClick={() => setActiveSection(section.id)}
            className={`w-full flex items-center gap-2 px-3 py-2.5 rounded-xl text-sm font-medium text-left transition ${
              activeSection === section.id
                ? 'bg-indigo-50 text-indigo-700'
                : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
            }`}
          >
            <span className="text-base">{section.icon}</span>
            {section.label}
          </button>
        ))}
      </div>

      {/* Editor area */}
      <div className="flex-1 min-w-0 card p-5">
        <h3 className="text-base font-semibold text-slate-800 mb-4 flex items-center gap-2">
          <span>{active?.icon}</span> {active?.label} Section
        </h3>
        {ActiveComponent && <ActiveComponent />}
      </div>
    </div>
  );
}
