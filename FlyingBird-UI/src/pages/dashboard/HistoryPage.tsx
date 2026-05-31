import React from 'react';
import { History } from 'lucide-react';

const HistoryPage: React.FC = () => {
  return (
    <div className="page-placeholder">
      <div className="page-placeholder-icon">
        <History size={48} />
      </div>
      <h2>History</h2>
      <p>Past transactions and event logs will appear here.</p>
    </div>
  );
};

export default HistoryPage;
