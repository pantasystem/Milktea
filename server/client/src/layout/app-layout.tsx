import { ReactNode} from "react";
import React from "react";

type AppLayoutProps = {
    children: React.ReactElement,
    sideBar?: ReactNode,

}

const AppNavHead: React.FC = () => {
  return (
  <div className="text-4xl text-white pl-4 pr-4">
    Milktea
  </div>
  )
}



const AppLayout: React.FC<AppLayoutProps> = ({children, sideBar}) => {
    return (
    <div className="flex h-screen w-screen">
      {
        sideBar && (
          <div className="w-64 bg-sky-900 pt-4">
            <AppNavHead />
            <div className="text-slate-200 pt-2">
              {sideBar}
            </div>
          </div>
        )
      }
      <div className="flex-1">
      {children}
      </div>
    </div>
    )
}

export default AppLayout;