import React from 'react';
import { ArrowLeftRight } from 'lucide-react';

const TradesPage: React.FC = () => {
  return (
    <div className="page-placeholder">
      <div className="page-placeholder-icon">
        <ArrowLeftRight size={48} />
      </div>
      <h2>Trades</h2>
      <p>Live and executed trade records will appear here.</p>
    </div>
  );
};

export default TradesPage;
