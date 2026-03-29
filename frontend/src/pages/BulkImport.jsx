import { useState, useRef } from 'react';
import Layout from '../components/Layout';
import { bulkImportService } from '../services/api';

export default function BulkImport() {
  const [result, setResult] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [dragging, setDragging] = useState(false);
  const fileRef = useRef();

  const handleFile = async (file) => {
    if (!file) return;
    if (!file.name.match(/\.(xlsx|xls)$/i)) {
      setError('Please upload an Excel file (.xlsx or .xls)');
      return;
    }
    setError('');
    setResult(null);
    setUploading(true);
    try {
      const res = await bulkImportService.upload(file);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Import failed');
    } finally {
      setUploading(false);
    }
  };

  const onDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    const file = e.dataTransfer.files?.[0];
    handleFile(file);
  };

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-slate-900">Bulk Import</h1>
          <p className="text-slate-500 mt-1">Upload an Excel file to import multiple students at once</p>
        </div>

        {/* Template info */}
        <div className="card p-5 mb-6 border-l-4 border-indigo-500">
          <h3 className="font-semibold text-slate-900 mb-2">📋 Required Excel Format</h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 text-xs">
            {['Name', 'Phone Number', 'Alternate Number', 'Standard/Course', 'Address', "Guardian's Name", 'Student ID (optional)'].map(col => (
              <span key={col} className="px-2 py-1 bg-slate-100 text-slate-600 rounded font-mono">{col}</span>
            ))}
          </div>
        </div>

        {/* Upload area */}
        <div
          className={`card p-8 mb-6 flex flex-col items-center text-center border-2 border-dashed cursor-pointer transition ${
            dragging ? 'border-indigo-400 bg-indigo-50' : 'border-slate-200 hover:border-indigo-300 hover:bg-indigo-50/30'
          }`}
          onDragOver={e => { e.preventDefault(); setDragging(true); }}
          onDragLeave={() => setDragging(false)}
          onDrop={onDrop}
          onClick={() => fileRef.current?.click()}
        >
          <div className="w-16 h-16 bg-indigo-100 rounded-2xl flex items-center justify-center text-3xl mb-4">
            📊
          </div>
          <p className="text-slate-900 font-semibold mb-1">
            {uploading ? 'Uploading...' : 'Drop your Excel file here'}
          </p>
          <p className="text-slate-400 text-sm">or click to browse (.xlsx, .xls)</p>
          {uploading && (
            <div className="mt-4 w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          )}
          <input ref={fileRef} type="file" accept=".xlsx,.xls" className="hidden"
            onChange={e => handleFile(e.target.files?.[0])} />
        </div>

        {error && (
          <div className="mb-4 px-4 py-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
            ⚠️ {error}
          </div>
        )}

        {/* Results */}
        {result && (
          <div className="card">
            <div className={`px-6 py-4 rounded-t-xl ${result.success ? 'bg-emerald-50' : 'bg-amber-50'}`}>
              <h3 className={`font-semibold ${result.success ? 'text-emerald-700' : 'text-amber-700'}`}>
                {result.success ? '✅ Import Successful' : '⚠️ Import Completed with Errors'}
              </h3>
            </div>
            <div className="p-6">
              <div className="grid grid-cols-3 gap-4 mb-4">
                <div className="text-center p-3 bg-slate-50 rounded-xl">
                  <p className="text-2xl font-bold text-slate-900">{result.totalRecords}</p>
                  <p className="text-xs text-slate-500 mt-1">Total Rows</p>
                </div>
                <div className="text-center p-3 bg-emerald-50 rounded-xl">
                  <p className="text-2xl font-bold text-emerald-600">{result.successfulImports}</p>
                  <p className="text-xs text-slate-500 mt-1">Imported</p>
                </div>
                <div className="text-center p-3 bg-red-50 rounded-xl">
                  <p className="text-2xl font-bold text-red-600">{result.failedImports}</p>
                  <p className="text-xs text-slate-500 mt-1">Failed</p>
                </div>
              </div>
              {result.errors?.length > 0 && (
                <div>
                  <p className="text-sm font-medium text-slate-700 mb-2">Errors:</p>
                  <div className="space-y-1 max-h-40 overflow-y-auto">
                    {result.errors.map((err, i) => (
                      <p key={i} className="text-xs text-red-600 bg-red-50 px-3 py-1.5 rounded">{err}</p>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
