/**
 * useSiteConfig – hook for promo-site components to consume the site configuration.
 *
 * Usage:
 *   const { featureConfig, contentConfig, loading } = useSiteConfig();
 *   if (!featureConfig.hero) return null;
 *   return <Hero data={contentConfig} />;
 */
import { useState, useEffect } from 'react';
import { configService } from '../services/configService';

export function useSiteConfig() {
  const [featureConfig, setFeatureConfig] = useState(null);
  const [contentConfig, setContentConfig] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    configService.getPublicConfig()
      .then((config) => {
        setFeatureConfig(config.featureConfig);
        setContentConfig(config.contentConfig);
      })
      .catch((err) => {
        console.error('[useSiteConfig] Failed to load config', err);
        setError(err);
      })
      .finally(() => setLoading(false));
  }, []);

  return { featureConfig, contentConfig, loading, error };
}
