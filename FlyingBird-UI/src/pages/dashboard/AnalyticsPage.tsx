import React from 'react';
import { BarChart2 } from 'lucide-react';

const AnalyticsPage: React.FC = () => {
  return (
    <div className="page-placeholder">
      <div className="page-placeholder-icon">
        <BarChart2 size={48} />
      </div>
      <h2>Analytics</h2>
      <p>Performance charts and market analytics will appear here.</p>
    </div>
  );
};

export default AnalyticsPage;
