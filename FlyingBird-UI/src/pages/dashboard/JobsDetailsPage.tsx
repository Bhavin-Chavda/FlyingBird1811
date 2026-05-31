import React from 'react';
import { Briefcase } from 'lucide-react';

const JobsDetailsPage: React.FC = () => {
  return (
    <div className="page-placeholder">
      <div className="page-placeholder-icon">
        <Briefcase size={48} />
      </div>
      <h2>Jobs Details</h2>
      <p>Active and scheduled job configurations will appear here.</p>
    </div>
  );
};

export default JobsDetailsPage;
