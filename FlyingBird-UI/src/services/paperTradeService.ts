import api from './api';
import type { PaperTrade } from '../types/paperTrade';

export interface PaperTradeFilters {
  status?: string;
  timeframe?: string;
  tradeType?: string;
  patternName?: string;
  safeTrade?: boolean;
  fromDate?: string; // yyyy-MM-dd
  toDate?: string;   // yyyy-MM-dd
}

/**
 * Fetch paper trades (JWT attached by the axios interceptor).
 * GET /api/paper-trades — optional server-side filters; the page also filters client-side.
 */
export const getPaperTrades = async (filters?: PaperTradeFilters): Promise<PaperTrade[]> => {
  const params: Record<string, string | boolean> = {};
  if (filters) {
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params[k] = v as string | boolean;
      }
    });
  }
  const response = await api.get<PaperTrade[]>('/api/paper-trades', { params });
  return response.data;
};
