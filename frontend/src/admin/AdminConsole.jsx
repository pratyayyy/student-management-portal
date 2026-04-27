/**
 * AdminConsole – main layout for the promotional website admin console.
 *
 * Sidebar navigation: Features | Content | Media | Preview
 * Loads config from backend on mount; persists changes in real time.
 */
import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import useAdminConfigStore from '../store/adminConfigStore';
import FeatureToggles from './components/FeatureToggles';
import ContentEditor from './components/ContentEditor';
import PreviewPanel from './components/PreviewPanel';
import MediaTab from './components/MediaTab';

const TABS = [
  { id: 'features', label: 'Features', icon: '⚙️',  description: 'Toggle sections on / off' },
  { id: 'content',  label: 'Content',  icon: '✏️',  description: 'Edit text and list content' },
  { id: 'media',    label: 'Media',    icon: '🖼️', description: 'Manage images and uploads' },
  { id: 'preview',  label: 'Preview',  icon: '👁️', description: 'Live preview of the site' },
];

export default function AdminConsole() {
  const [activeTab, setActiveTab] = useState('features');
  const { loading, error, lastSaved, loadFromBackend } = useAdminConfigStore();

  // Load config from backend when the console mounts.
  useEffect(() => {
    loadFromBackend();
  }, [loadFromBackend]);

  return (
    <Layout>
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Admin Console</h1>
            <p className="text-slate-500 mt-0.5 text-sm">
              Configure and manage the promotional website in real time.
            </p>
          </div>
          <div className="flex items-center gap-3">
            {lastSaved && (
              <span className="text-xs text-slate-400">
                Last saved: {lastSaved.toLocaleTimeString()}
              </span>
            )}
            {error && (
              <span className="text-xs text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-1">
                ⚠️ {error}
              </span>
            )}
          </div>
        </div>

        {/* Tab nav */}
        <div className="flex gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit flex-wrap">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition ${
                activeTab === tab.id
                  ? 'bg-white text-indigo-700 shadow-sm'
                  : 'text-slate-500 hover:text-slate-700'
              }`}
            >
              <span>{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>

        {/* Loading state (skip for media tab which loads independently) */}
        {loading && activeTab !== 'media' && (
          <div className="flex justify-center py-20">
            <div className="flex flex-col items-center gap-3">
              <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
              <p className="text-sm text-slate-500">Loading configuration…</p>
            </div>
          </div>
        )}

        {/* Tab content */}
        {(!loading || activeTab === 'media') && (
          <>
            {activeTab === 'features' && (
              <div>
                <div className="mb-4">
                  <h2 className="text-lg font-semibold text-slate-800">Feature Toggles</h2>
                  <p className="text-sm text-slate-500">
                    Enable or disable sections of the promotional website. Changes are saved instantly.
                  </p>
                </div>
                <FeatureToggles />
              </div>
            )}

            {activeTab === 'content' && (
              <div>
                <div className="mb-4">
                  <h2 className="text-lg font-semibold text-slate-800">Content Management</h2>
                  <p className="text-sm text-slate-500">
                    Edit the text, lists, and structured data displayed on each section.
                  </p>
                </div>
                <ContentEditor />
              </div>
            )}

            {activeTab === 'media' && (
              <div>
                <div className="mb-4">
                  <h2 className="text-lg font-semibold text-slate-800">Media Library</h2>
                  <p className="text-sm text-slate-500">
                    Upload and manage images used throughout the promotional website.
                  </p>
                </div>
                <MediaTab />
              </div>
            )}

            {activeTab === 'preview' && (
              <div>
                <div className="mb-4">
                  <h2 className="text-lg font-semibold text-slate-800">Live Preview</h2>
                  <p className="text-sm text-slate-500">
                    See how the promotional website looks with the current configuration.
                  </p>
                </div>
                <PreviewPanel />
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  );
}
