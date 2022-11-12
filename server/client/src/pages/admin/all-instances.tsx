import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import InstancesStatePage from "./components/instances-state-page";
import React from "react";
import ScrollLayout from "../../layout/scroll-layout";
import { Link } from "react-router-dom";

const AllInstancesPage: React.FC = () => {
  return <BodyLayout topAppBar={
      <AppBarLayout actions={
        <div>
          <Link to={`/admin/instances/create`}><p className="bg-sky-600 p-1 rounded-lg text-white hover:bg-sky-500">追加</p></Link>
        </div>
      }>
        全てのインスタンス
      </AppBarLayout>
    }>
      <ScrollLayout>
        <InstancesStatePage filterType="all" />
      </ScrollLayout>
    </BodyLayout>
}
export default AllInstancesPage;