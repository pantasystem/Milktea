import AppBarLayout from "../../layout/AppBarLayout";
import BodyLayout from "../../layout/BodyLayout";
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