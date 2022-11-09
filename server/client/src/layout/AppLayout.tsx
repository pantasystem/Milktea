import { Link } from "react-router-dom"
import SideMenuItemLayout from "./SideMenuItemLayout"

type AppLayoutProps = {
    children: React.ReactElement
}

const AppNavHead: React.FC = () => {
  return (
  <div className="text-4xl text-white pl-4 pr-4">
    Milktea
  </div>
  )
}



const AppLayout: React.FC<AppLayoutProps> = ({children}) => {
    return (
    <div className="flex h-screen w-screen">
      <div className="w-64 bg-sky-900 pt-4">
        <AppNavHead />
        <div className="text-slate-200 pt-2">
          <SideMenuItemLayout to="/admin/all-instances">
          全インスタンス
          </SideMenuItemLayout>
          <SideMenuItemLayout to="/admin/approved-instances">
          承認済み
          </SideMenuItemLayout>
          <SideMenuItemLayout to="/admin/unapproved-instances">
          未承認
          </SideMenuItemLayout>
          <SideMenuItemLayout to="/admin/blacklist">
          ブラックリスト
          </SideMenuItemLayout>
          <SideMenuItemLayout to="/admin/account">
          アカウント
          </SideMenuItemLayout>
        </div>
        
      </div>
      <div className="flex-auto p-4">
        {children}
      </div>
    </div>
    )
}

export default AppLayout;