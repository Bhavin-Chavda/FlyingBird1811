import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import OverviewPage from './pages/dashboard/OverviewPage';
import JobsDetailsPage from './pages/dashboard/JobsDetailsPage';
import TradesPage from './pages/dashboard/TradesPage';
import HistoryPage from './pages/dashboard/HistoryPage';
import AnalyticsPage from './pages/dashboard/AnalyticsPage';

function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            >
              <Route index element={<OverviewPage />} />
              <Route path="jobs-details" element={<JobsDetailsPage />} />
              <Route path="trades"       element={<TradesPage />} />
              <Route path="history"      element={<HistoryPage />} />
              <Route path="analytics"    element={<AnalyticsPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ToastProvider>
  );
}

export default App;
