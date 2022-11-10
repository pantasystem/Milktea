import React from "react";
import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import InstancesStatePage from "./components/instances-state-page";


const ApprovedInstancesPage: React.FC = () => {
    return <BodyLayout topAppBar={
        <AppBarLayout>
          公開承認済みインスタンス
        </AppBarLayout>
      }>
        <InstancesStatePage filterType="approved" />
      </BodyLayout>
}
export default ApprovedInstancesPage