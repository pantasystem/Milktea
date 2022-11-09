import React from 'react';
import {
  createBrowserRouter,
  RouterProvider,
  Route,
  Outlet,
} from "react-router-dom";
import AppLayout from './layout/AppLayout';
const AdminRootPage: React.FC = () => {
  return (
    <AppLayout children={
      <Outlet />
    }/>
  )
}
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
        element: <div>Hoge</div>
      },
    ]
  }
]);


function App() {
  return (
    <RouterProvider router={router} />
  );
}

export default App;
