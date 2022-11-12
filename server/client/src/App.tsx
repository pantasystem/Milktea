import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import {
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";
import AdminRootPage from './pages/admin/admin-root-page';
import AllInstancesPage from './pages/admin/all-instances';
import ApprovedInstancesPage from './pages/admin/approved-instances-page';
import InstanceDetailPage from './pages/admin/instance-detail-page';
import { InstanceRegisterPage } from './pages/admin/instance-register-page';
import UnapprovedInstancesPage from './pages/admin/unapproved-instances-page';

export const queryClient = new QueryClient();

const router = createBrowserRouter([
  {
    path: "/",
    element: <div>Home</div>
  },
  {
    path: "/admin",
    element: <AdminRootPage />,
    errorElement: <div>Not Found</div>,
    children: [
      {
        path: "",
        element: <div>管理画面</div>
      },
      {
        path: "all-instances",
        element: <AllInstancesPage />
      },
      {
        path: "approved-instances",
        element: <ApprovedInstancesPage />
      },
      {
        path: "unapproved-instances",
        element: <UnapprovedInstancesPage />
      },
      {
        path: "blacklist",
        element: <div>Blacklist</div>
      },
      {
        path: "account",
        element: <div>Account</div>
      },
      {
        path: "instances/:instanceId",
        element: <InstanceDetailPage />,
      },
      {
        path: "instances/create",
        element: <InstanceRegisterPage />

      }
    ]
  }
]);


function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}

export default App;
