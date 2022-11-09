import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import InstancesStatePage from "./components/instances-state-page";


const AllInstancesPage: React.FC = () => {
  return <BodyLayout topAppBar={
      <AppBarLayout>
        全てのインスタンス
      </AppBarLayout>
    }>
      <InstancesStatePage filterType="all" />
    </BodyLayout>
}
export default AllInstancesPage;