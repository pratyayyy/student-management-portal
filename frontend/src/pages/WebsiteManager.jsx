import { useState, useEffect, useRef } from 'react';
import Layout from '../components/Layout';
import { websiteService } from '../services/api';

const IMAGE_TYPES = ['HERO', 'ABOUT', 'GALLERY', 'LOGO'];

const CONTENT_GROUPS = [
  {
    title: 'Hero Section',
    keys: ['hero.title', 'hero.subtitle', 'hero.cta'],
  },
  {
    title: 'About Section',
    keys: ['about.heading', 'about.para1', 'about.para2', 'about.mission'],
  },
  {
    title: 'Contact Details',
    keys: ['contact.phone', 'contact.email', 'contact.address', 'contact.hours'],
  },
  {
    title: 'Statistics / Highlights',
    keys: ['stats.students', 'stats.experience', 'stats.placement'],
  },
];

// ── Sub-component: image drop-zone ────────────────────────────────────────────

function ImageUploadZone({ onUpload, uploading }) {
  const [dragging, setDragging] = useState(false);
  const fileRef = useRef();

  const handleFile = (file) => {
    if (!file) return;
    if (!file.type.startsWith('image/')) return;
    onUpload(file);
  };

  return (
    <div
      className={`border-2 border-dashed rounded-xl p-6 flex flex-col items-center text-center cursor-pointer transition ${
        dragging ? 'border-indigo-400 bg-indigo-50' : 'border-slate-200 hover:border-indigo-300 hover:bg-indigo-50/40'
      }`}
      onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
      onDragLeave={() => setDragging(false)}
      onDrop={(e) => { e.preventDefault(); setDragging(false); handleFile(e.dataTransfer.files?.[0]); }}
      onClick={() => fileRef.current?.click()}
    >
      <div className="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center text-2xl mb-3">🖼️</div>
      {uploading
        ? <div className="w-6 h-6 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
        : <>
            <p className="text-sm font-medium text-slate-700">Drop image here or click to browse</p>
            <p className="text-xs text-slate-400 mt-1">JPG, PNG, GIF · max 5 MB</p>
          </>
      }
      <input ref={fileRef} type="file" accept="image/*" className="hidden"
        onChange={(e) => handleFile(e.target.files?.[0])} />
    </div>
  );
}

// ── Sub-component: single image card ─────────────────────────────────────────

function ImageCard({ image, onDelete, onUpdateMeta }) {
  const [altText, setAltText] = useState(image.altText || '');
  const [sortOrder, setSortOrder] = useState(image.sortOrder ?? 0);
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await onUpdateMeta(image.id, { altText, sortOrder });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="card p-4 flex flex-col gap-3">
      <img
        src={image.fileUrl}
        alt={image.altText || 'site image'}
        className="w-full h-40 object-cover rounded-lg bg-slate-100"
      />
      <div className="space-y-2">
        <input
          className="input w-full text-xs"
          placeholder="Alt text (accessibility)"
          value={altText}
          onChange={(e) => setAltText(e.target.value)}
        />
        <div className="flex items-center gap-2">
          <label className="text-xs text-slate-500 w-20">Sort order</label>
          <input
            type="number"
            className="input w-20 text-xs"
            value={sortOrder}
            onChange={(e) => setSortOrder(Number(e.target.value))}
          />
          <button
            onClick={handleSave}
            disabled={saving}
            className="ml-auto text-xs px-3 py-1.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition"
          >
            {saving ? 'Saving…' : 'Save'}
          </button>
          <button
            onClick={() => onDelete(image.id)}
            className="text-xs px-3 py-1.5 bg-red-50 text-red-600 border border-red-200 rounded-lg hover:bg-red-100 transition"
          >
            Delete
          </button>
        </div>
        <p className="text-xs text-slate-400">{image.originalFileName} · {Math.round((image.compressedSize || 0) / 1024)} KB compressed</p>
      </div>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────

export default function WebsiteManager() {
  const [activeTab, setActiveTab] = useState('content');
  const [activeImageType, setActiveImageType] = useState('HERO');

  // Content state
  const [contentMap, setContentMap] = useState({});        // key → value
  const [labelMap, setLabelMap] = useState({});            // key → label
  const [contentLoading, setContentLoading] = useState(true);
  const [contentSaving, setContentSaving] = useState(false);
  const [contentSuccess, setContentSuccess] = useState(false);

  // Images state
  const [images, setImages] = useState([]);                // all images from server
  const [imagesLoading, setImagesLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [imageError, setImageError] = useState('');

  // ── Data loading ──────────────────────────────────────────────────────────

  useEffect(() => {
    websiteService.getContent()
      .then((res) => {
        const cm = {};
        const lm = {};
        res.data.forEach((item) => {
          cm[item.contentKey] = item.contentValue || '';
          lm[item.contentKey] = item.label || item.contentKey;
        });
        setContentMap(cm);
        setLabelMap(lm);
      })
      .catch(console.error)
      .finally(() => setContentLoading(false));
  }, []);

  useEffect(() => {
    setImagesLoading(true);
    websiteService.getAllImages()
      .then((res) => setImages(res.data))
      .catch(console.error)
      .finally(() => setImagesLoading(false));
  }, []);

  // ── Content handlers ──────────────────────────────────────────────────────

  const handleContentChange = (key, value) => {
    setContentMap((prev) => ({ ...prev, [key]: value }));
  };

  const handleContentSave = async () => {
    setContentSaving(true);
    setContentSuccess(false);
    try {
      await websiteService.updateContent(contentMap);
      setContentSuccess(true);
      setTimeout(() => setContentSuccess(false), 3000);
    } catch (err) {
      console.error(err);
    } finally {
      setContentSaving(false);
    }
  };

  // ── Image handlers ────────────────────────────────────────────────────────

  const handleUpload = async (file) => {
    setUploading(true);
    setImageError('');
    try {
      const res = await websiteService.uploadImage(file, activeImageType, '', 0);
      setImages((prev) => [...prev, res.data.image]);
    } catch (err) {
      setImageError(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this image?')) return;
    try {
      await websiteService.deleteImage(id);
      setImages((prev) => prev.filter((img) => img.id !== id));
    } catch (err) {
      console.error(err);
    }
  };

  const handleUpdateMeta = async (id, data) => {
    try {
      const res = await websiteService.updateImageMeta(id, data);
      setImages((prev) => prev.map((img) => (img.id === id ? res.data : img)));
    } catch (err) {
      console.error(err);
    }
  };

  const filteredImages = images.filter((img) => img.imageType === activeImageType);

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <Layout>
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-slate-900">Website Manager</h1>
          <p className="text-slate-500 mt-1">Configure the content and images displayed on the promotional website.</p>
        </div>

        {/* Tabs */}
        <div className="flex gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
          {[
            { id: 'content', label: '✏️ Text Content' },
            { id: 'images', label: '🖼️ Images' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                activeTab === tab.id
                  ? 'bg-white text-indigo-700 shadow-sm'
                  : 'text-slate-500 hover:text-slate-700'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* ── TEXT CONTENT TAB ── */}
        {activeTab === 'content' && (
          <>
            {contentLoading ? (
              <div className="flex justify-center py-20">
                <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
              </div>
            ) : (
              <div className="space-y-6">
                {CONTENT_GROUPS.map((group) => (
                  <div key={group.title} className="card">
                    <div className="px-6 py-4 border-b border-slate-100">
                      <h2 className="font-semibold text-slate-900">{group.title}</h2>
                    </div>
                    <div className="p-6 space-y-4">
                      {group.keys.map((key) => (
                        <div key={key}>
                          <label className="block text-sm font-medium text-slate-700 mb-1">
                            {labelMap[key] || key}
                            <span className="ml-2 text-xs text-slate-400 font-mono">{key}</span>
                          </label>
                          {(key.includes('para') || key.includes('mission') || key.includes('subtitle') || key.includes('address')) ? (
                            <textarea
                              rows={3}
                              className="input w-full resize-y"
                              value={contentMap[key] || ''}
                              onChange={(e) => handleContentChange(key, e.target.value)}
                            />
                          ) : (
                            <input
                              type="text"
                              className="input w-full"
                              value={contentMap[key] || ''}
                              onChange={(e) => handleContentChange(key, e.target.value)}
                            />
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                ))}

                <div className="flex items-center gap-4">
                  <button
                    onClick={handleContentSave}
                    disabled={contentSaving}
                    className="btn-primary px-6 py-2.5 text-sm"
                  >
                    {contentSaving ? 'Saving…' : 'Save All Changes'}
                  </button>
                  {contentSuccess && (
                    <span className="text-sm text-emerald-600 font-medium">✅ Saved successfully!</span>
                  )}
                </div>
              </div>
            )}
          </>
        )}

        {/* ── IMAGES TAB ── */}
        {activeTab === 'images' && (
          <>
            {/* Image type selector */}
            <div className="flex flex-wrap gap-2 mb-6">
              {IMAGE_TYPES.map((type) => (
                <button
                  key={type}
                  onClick={() => setActiveImageType(type)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium border transition ${
                    activeImageType === type
                      ? 'bg-indigo-600 text-white border-indigo-600'
                      : 'bg-white text-slate-600 border-slate-200 hover:border-indigo-300'
                  }`}
                >
                  {type}
                  <span className="ml-2 text-xs opacity-70">
                    ({images.filter((img) => img.imageType === type).length})
                  </span>
                </button>
              ))}
            </div>

            {/* Type description */}
            <div className="card p-4 mb-6 border-l-4 border-indigo-500">
              <p className="text-sm text-slate-600">
                {{
                  HERO: '🌟 Hero – the main banner image at the top of the promotional page. Recommended: 1920×600 px.',
                  ABOUT: '📖 About – image(s) shown alongside the about section text. Recommended: 800×600 px.',
                  GALLERY: '🖼️ Gallery – multiple images shown in a grid or carousel. Any aspect ratio.',
                  LOGO: '🏫 Logo – the institute logo. Recommended: square PNG with transparent background.',
                }[activeImageType]}
              </p>
            </div>

            {/* Upload zone */}
            <div className="mb-6">
              <ImageUploadZone onUpload={handleUpload} uploading={uploading} />
              {imageError && (
                <p className="mt-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                  ⚠️ {imageError}
                </p>
              )}
            </div>

            {/* Image grid */}
            {imagesLoading ? (
              <div className="flex justify-center py-12">
                <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
              </div>
            ) : filteredImages.length === 0 ? (
              <div className="card py-12 text-center text-slate-400">
                <p className="text-4xl mb-3">📭</p>
                <p>No {activeImageType.toLowerCase()} images uploaded yet</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredImages.map((image) => (
                  <ImageCard
                    key={image.id}
                    image={image}
                    onDelete={handleDelete}
                    onUpdateMeta={handleUpdateMeta}
                  />
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  );
}
