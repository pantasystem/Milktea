import { useEffect } from "react";
import { Outlet } from "react-router";
import { useRecoilState } from "recoil";
import AppLayout from "../../layout/app-layout";
import SideMenuItemLayout from "../../layout/side-menu-item-layout";
import { AccountSchema } from "../../models/account";
import { authAtom, AuthState } from "../../state/auth";
import LoginPage from "./login";
import React from "react";




const AutohrizedSideMenu: React.FC = () => {
  return (
    <div>
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
  );
};

type AdminRootPageLayoutProps = {
  state: AuthState
}
const AdminRootPageLayout: React.FC<AdminRootPageLayoutProps> = ({state}) => {
  if (state === "Loading") {
    return <div>Loading...</div>
  } else {
    return <AppLayout
      children={
        state === "Authorized" ? <Outlet /> : <LoginPage />
      }
      sideBar={
        state === "Authorized" ? <AutohrizedSideMenu /> : undefined
      }
    />
  }
}
const AdminRootPage: React.FC = () => {
  const [authState, setAuthState] = useRecoilState(authAtom)
  useEffect(() => {
    const currentAccount = async() => {
      const res = await fetch("/api/admin/accounts/current", {
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("Token")}`
        }
      })
      const result = await AccountSchema.safeParseAsync(await res.json())
      if (result.success) {
        setAuthState("Authorized");
      } else {
        if (res.status === 401) {
          setAuthState("Unauthorized");
        }
      }
    }
    currentAccount();
  }, [setAuthState]);
  
  return (
    <AdminRootPageLayout state={authState} />
  )
}

export default AdminRootPage;