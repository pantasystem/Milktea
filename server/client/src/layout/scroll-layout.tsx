import React, { ReactNode } from "react";

type Props = {
    children: ReactNode
}
const ScrollLayout: React.FC<Props> = ({children}) => {
  return <div className="overflow-y-scroll">
    {children}
  </div>
}

export default ScrollLayout;