import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import InstancesStatePage from "./components/instances-state-page";
import React from "react";
import ScrollLayout from "../../layout/scroll-layout";

const AllInstancesPage: React.FC = () => {
  return <BodyLayout topAppBar={
      <AppBarLayout>
        全てのインスタンス
      </AppBarLayout>
    }>
      <ScrollLayout>
        <InstancesStatePage filterType="all" />
      </ScrollLayout>
    </BodyLayout>
}
export default AllInstancesPage;