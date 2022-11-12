import React, { ReactNode } from "react";

type Props = {
  children: ReactNode,
  actions?: ReactNode,
}
const AppBarLayout: React.FC<Props> = ({ children, actions }) => {
  return <div className="drop-shadow-md w-full bg-white p-3 flex justify-between items-center">
    <div className="text-2xl">
      {children}
    </div>
    <div>
      {actions}
    </div>
  </div>
   
}

export default AppBarLayout;