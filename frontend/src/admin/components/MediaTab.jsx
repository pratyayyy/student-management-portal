/**
 * MediaTab – image management tab embedded inside the Admin Console.
 * Lifted from WebsiteManager to avoid Layout nesting issues.
 */
import { useState, useEffect, useRef } from 'react';
import { websiteService } from '../../services/api';

const IMAGE_TYPES = ['HERO', 'ABOUT', 'GALLERY', 'LOGO'];

function ImageUploadZone({ onUpload, uploading }) {
  const [dragging, setDragging] = useState(false);
  const fileRef = useRef();

  const handleFile = (file) => {
    if (!file || !file.type.startsWith('image/')) return;
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

function ImageCard({ image, onDelete, onUpdateMeta }) {
  const [altText, setAltText] = useState(image.altText || '');
  const [sortOrder, setSortOrder] = useState(image.sortOrder ?? 0);
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try { await onUpdateMeta(image.id, { altText, sortOrder }); }
    finally { setSaving(false); }
  };

  return (
    <div className="card p-4 flex flex-col gap-3">
      <img src={image.fileUrl} alt={image.altText || 'site image'} className="w-full h-40 object-cover rounded-lg bg-slate-100" />
      <div className="space-y-2">
        <input className="input w-full text-xs" placeholder="Alt text" value={altText} onChange={(e) => setAltText(e.target.value)} />
        <div className="flex items-center gap-2">
          <label className="text-xs text-slate-500 w-20">Sort order</label>
          <input type="number" className="input w-20 text-xs" value={sortOrder} onChange={(e) => setSortOrder(Number(e.target.value))} />
          <button onClick={handleSave} disabled={saving} className="ml-auto text-xs px-3 py-1.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition">
            {saving ? 'Saving…' : 'Save'}
          </button>
          <button onClick={() => onDelete(image.id)} className="text-xs px-3 py-1.5 bg-red-50 text-red-600 border border-red-200 rounded-lg hover:bg-red-100 transition">
            Delete
          </button>
        </div>
        <p className="text-xs text-slate-400">{image.originalFileName} · {((image.compressedSize || 0) / 1024).toFixed(1)} KB</p>
      </div>
    </div>
  );
}

export default function MediaTab() {
  const [activeType, setActiveType] = useState('HERO');
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [imageError, setImageError] = useState('');

  useEffect(() => {
    setLoading(true);
    websiteService.getAllImages()
      .then((res) => setImages(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleUpload = async (file) => {
    setUploading(true);
    setImageError('');
    try {
      const res = await websiteService.uploadImage(file, activeType, '', 0);
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
    } catch (err) { console.error(err); }
  };

  const handleUpdateMeta = async (id, data) => {
    try {
      const res = await websiteService.updateImageMeta(id, data);
      setImages((prev) => prev.map((img) => (img.id === id ? res.data : img)));
    } catch (err) { console.error(err); }
  };

  const filtered = images.filter((img) => img.imageType === activeType);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap gap-2">
        {IMAGE_TYPES.map((type) => (
          <button key={type} onClick={() => setActiveType(type)}
            className={`px-4 py-2 rounded-lg text-sm font-medium border transition ${
              activeType === type ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-slate-600 border-slate-200 hover:border-indigo-300'
            }`}>
            {type}
            <span className="ml-1 text-xs opacity-70">({images.filter((i) => i.imageType === type).length})</span>
          </button>
        ))}
      </div>
      <ImageUploadZone onUpload={handleUpload} uploading={uploading} />
      {imageError && (
        <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">⚠️ {imageError}</p>
      )}
      {loading ? (
        <div className="flex justify-center py-12">
          <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="card py-12 text-center text-slate-400">
          <p className="text-4xl mb-3">📭</p>
          <p>No {activeType.toLowerCase()} images uploaded yet</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((image) => (
            <ImageCard key={image.id} image={image} onDelete={handleDelete} onUpdateMeta={handleUpdateMeta} />
          ))}
        </div>
      )}
    </div>
  );
}
