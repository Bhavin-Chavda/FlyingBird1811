// Central, single source of truth for the 3 scheduler jobs shown on the
// Job Details page. Job ids / timeframe codes mirror the backend enums
// (JobId, Timeframe) exactly — do not invent values.

export interface JobConfig {
  /** Canonical backend job id (matches JobId.getCode()). */
  jobId: string;
  /** Market-data timeframe code used in the API path (matches Timeframe.getCode()). */
  timeframe: string;
  /** Human-readable card title. */
  label: string;
}

export const JOB_CONFIGS: JobConfig[] = [
  { jobId: 'fb_1m_job',  timeframe: '1m',  label: '1 Minute Candle'  },
  { jobId: 'fb_5m_job',  timeframe: '5m',  label: '5 Minute Candle'  },
  { jobId: 'fb_15m_job', timeframe: '15m', label: '15 Minute Candle' },
  { jobId: 'fb_1h_job',  timeframe: '1h',  label: '1 Hour Candle'    },
];
