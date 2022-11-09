import AppBarLayout from "../../layout/AppBarLayout";
import BodyLayout from "../../layout/BodyLayout";

const AllInstancesPage: React.FC = () => {
    return <BodyLayout topAppBar={
        <AppBarLayout>
            全てのインスタンス
        </AppBarLayout>
    }>
        <div className="">
        Body
        </div>
    </BodyLayout>
}
export default AllInstancesPage;