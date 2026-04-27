/**
 * Zustand store for the Admin Console.
 *
 * Holds featureConfig (toggle map) and contentConfig (content key/value map).
 * Actions synchronise with the backend via configService.
 */
import { create } from 'zustand';
import { configService, defaultFeatureConfig, defaultContentConfig } from '../services/configService';

const useAdminConfigStore = create((set, get) => ({
  featureConfig: { ...defaultFeatureConfig },
  contentConfig: { ...defaultContentConfig },
  loading: false,
  saving: false,
  error: null,
  lastSaved: null,

  /** Toggle a single feature on or off (optimistic + persisted). */
  setFeatureToggle: async (key, value) => {
    // Optimistic update
    set((state) => ({
      featureConfig: { ...state.featureConfig, [key]: value },
    }));
    try {
      await configService.updateFeatures({ [key]: value });
    } catch (err) {
      console.error('[adminConfigStore] Failed to persist feature toggle', err);
      // Roll back
      set((state) => ({
        featureConfig: { ...state.featureConfig, [key]: !value },
        error: 'Failed to save feature toggle',
      }));
    }
  },

  /** Update a section of the content config (optimistic + persisted). */
  setContentSection: async (updates) => {
    // Optimistic update
    set((state) => ({
      contentConfig: { ...state.contentConfig, ...updates },
    }));
    try {
      set({ saving: true, error: null });
      await configService.updateContent(updates);
      set({ saving: false, lastSaved: new Date() });
    } catch (err) {
      console.error('[adminConfigStore] Failed to persist content', err);
      set({ saving: false, error: 'Failed to save content' });
    }
  },

  /** Load config from backend on mount. */
  loadFromBackend: async () => {
    set({ loading: true, error: null });
    try {
      const config = await configService.getConfig();
      set({
        featureConfig: config.featureConfig,
        contentConfig: config.contentConfig,
        loading: false,
      });
    } catch (err) {
      console.error('[adminConfigStore] Failed to load config', err);
      set({ loading: false, error: 'Failed to load configuration' });
    }
  },

  /** Save the full config object to backend. */
  saveToBackend: async () => {
    const { featureConfig, contentConfig } = get();
    set({ saving: true, error: null });
    try {
      await configService.saveConfig({ featureConfig, contentConfig });
      set({ saving: false, lastSaved: new Date() });
    } catch (err) {
      console.error('[adminConfigStore] Failed to save config', err);
      set({ saving: false, error: 'Failed to save configuration' });
    }
  },
}));

export default useAdminConfigStore;
