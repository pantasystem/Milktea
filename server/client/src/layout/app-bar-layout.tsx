import React, { ReactNode } from "react";

type Props = {
  children: ReactNode,
}
const AppBarLayout: React.FC<Props> = ({ children }) => {
  return <div className="drop-shadow-md w-full bg-white p-3">
    <div className="text-2xl">
      {children}
    </div>
  </div>
   
}

export default AppBarLayout;