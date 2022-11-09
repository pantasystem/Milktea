import React, { ReactNode } from "react";

const AppBarLayout: React.FC = ({ children }: {children?: ReactNode}) => {
  return <div>
    {children}
  </div>
}

export default AppBarLayout;