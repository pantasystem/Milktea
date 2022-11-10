import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import InstancesStatePage from "./components/instances-state-page";
import React from "react";


const UnapprovedInstancesPage: React.FC = () => {
    return <BodyLayout topAppBar={
        <AppBarLayout>
          公開未承認済みインスタンス
        </AppBarLayout>
      }>
        <InstancesStatePage filterType="unapproved" />
      </BodyLayout>
}
export default UnapprovedInstancesPage;