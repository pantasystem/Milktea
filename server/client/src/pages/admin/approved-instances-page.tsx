import React from "react";
import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import ScrollLayout from "../../layout/scroll-layout";
import InstancesStatePage from "./components/instances-state-page";


const ApprovedInstancesPage: React.FC = () => {
    return <BodyLayout topAppBar={
        <AppBarLayout>
          公開承認済みインスタンス
        </AppBarLayout>
      }>
        <ScrollLayout>
          <InstancesStatePage filterType="approved" />
        </ScrollLayout>
      </BodyLayout>
}
export default ApprovedInstancesPage