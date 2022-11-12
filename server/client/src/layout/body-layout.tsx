import { ReactNode } from "react";
import React from "react";

type Props = {
  children?: ReactNode;
  topAppBar?: ReactNode;
};
const BodyLayout: React.FC<Props> = (
  { children, topAppBar }
) => {
  return <div className="w-full h-full flex-col flex">
    <div>
      {topAppBar}
    </div>
    {children}
  </div>
}

export default BodyLayout;