import api from './api';
import type { JobDetailsResponseDto } from '../types/jobDetails';

/**
 * Fetch the aggregate details for one scheduler job by timeframe (1m / 5m / 15m).
 * JWT is attached automatically by the axios request interceptor.
 *
 * GET /api/jobs/{timeframe}/details
 */
export const getJobDetails = async (timeframe: string): Promise<JobDetailsResponseDto> => {
  const response = await api.get<JobDetailsResponseDto>(`/api/jobs/${timeframe}/details`);
  return response.data;
};
