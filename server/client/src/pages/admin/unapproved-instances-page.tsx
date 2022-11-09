import AppBarLayout from "../../layout/AppBarLayout";
import BodyLayout from "../../layout/BodyLayout";
import InstancesStatePage from "./components/instances-state-page";


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