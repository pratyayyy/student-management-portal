/**
 * Config Service – abstraction layer for all site configuration operations.
 *
 * Backed by the Spring Boot REST API with localStorage as a fallback cache.
 * Swappable for Firebase / Supabase by replacing this file only.
 */
import { websiteService } from './api';

const LS_KEY = 'siteConfig';

// Default feature configuration – all sections enabled out of the box.
export const defaultFeatureConfig = {
  hero: true,
  carousel: true,
  about: true,
  whyChooseUs: true,
  courses: true,
  results: true,
  faculty: true,
  testimonials: true,
  blog: true,
  resources: true,
  demoBooking: true,
  scholarship: true,
  faq: true,
  contact: true,
  whatsapp: true,
  leadForms: true,
  login: true,
};

// Default content configuration with empty / placeholder values.
export const defaultContentConfig = {
  'hero.title': 'Institute of Junior Accountants',
  'hero.subtitle': 'Shaping tomorrow\'s accounting professionals with world-class education.',
  'hero.cta': 'Enquire Now',
  'about.heading': 'About IJA',
  'about.para1': '',
  'about.para2': '',
  'about.mission': '',
  'contact.phone': '',
  'contact.email': '',
  'contact.address': '',
  'contact.hours': '',
  'stats.students': '2000+',
  'stats.experience': '20+',
  'stats.placement': '95%',
  'json.courses': '[]',
  'json.faculty': '[]',
  'json.testimonials': '[]',
  'json.blog': '[]',
  'json.results': '[]',
  'json.faq': '[]',
};

/** Persist config snapshot to localStorage for offline resilience. */
function persistToLocal(config) {
  try {
    localStorage.setItem(LS_KEY, JSON.stringify(config));
  } catch {
    // storage quota exceeded – ignore
  }
}

/** Load the last-known config from localStorage (may be stale). */
function loadFromLocal() {
  try {
    const raw = localStorage.getItem(LS_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export const configService = {
  /**
   * Fetch the full site config (featureConfig + contentConfig) from the backend.
   * Falls back to localStorage if the network call fails.
   */
  getConfig: async () => {
    try {
      const res = await websiteService.getConfig();
      const config = {
        featureConfig: { ...defaultFeatureConfig, ...(res.data.featureConfig || {}) },
        contentConfig: { ...defaultContentConfig, ...(res.data.contentConfig || {}) },
      };
      persistToLocal(config);
      return config;
    } catch (err) {
      console.warn('[configService] Backend unavailable, falling back to localStorage', err);
      return loadFromLocal() || { featureConfig: defaultFeatureConfig, contentConfig: defaultContentConfig };
    }
  },

  /** Save the full config to the backend and update the local cache. */
  saveConfig: async (config) => {
    const res = await websiteService.saveConfig(config);
    const saved = {
      featureConfig: { ...defaultFeatureConfig, ...(res.data.featureConfig || {}) },
      contentConfig: { ...defaultContentConfig, ...(res.data.contentConfig || {}) },
    };
    persistToLocal(saved);
    return saved;
  },

  /** Update feature toggles only. */
  updateFeatures: async (featureConfig) => {
    const res = await websiteService.updateFeatures(featureConfig);
    // Merge into existing local cache
    const local = loadFromLocal() || { featureConfig: defaultFeatureConfig, contentConfig: defaultContentConfig };
    const updated = { ...local, featureConfig: { ...defaultFeatureConfig, ...(res.data || {}) } };
    persistToLocal(updated);
    return updated.featureConfig;
  },

  /** Update a content section only. */
  updateContent: async (contentUpdates) => {
    await websiteService.updateContent(contentUpdates);
    const local = loadFromLocal() || { featureConfig: defaultFeatureConfig, contentConfig: defaultContentConfig };
    const updated = { ...local, contentConfig: { ...local.contentConfig, ...contentUpdates } };
    persistToLocal(updated);
    return updated.contentConfig;
  },

  /** Upload an image file and return the image DTO from the server. */
  uploadImage: async (file, imageType, altText = '', sortOrder = 0) => {
    const res = await websiteService.uploadImage(file, imageType, altText, sortOrder);
    return res.data.image;
  },

  /** Public: fetch config for the promo site (no auth required). */
  getPublicConfig: async () => {
    try {
      const res = await websiteService.getPublicConfig();
      return {
        featureConfig: { ...defaultFeatureConfig, ...(res.data.featureConfig || {}) },
        contentConfig: { ...defaultContentConfig, ...(res.data.contentConfig || {}) },
      };
    } catch {
      return loadFromLocal() || { featureConfig: defaultFeatureConfig, contentConfig: defaultContentConfig };
    }
  },
};
