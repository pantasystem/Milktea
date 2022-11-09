import { useMutation, useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import { queryClient } from "../../App";
import AppBarLayout from "../../layout/AppBarLayout";
import BodyLayout from "../../layout/BodyLayout";
import { Instance } from "../../models/instance";
import { InstanceRepository, instanceRepository } from "../../repositories/instance-repository";
// import { tokenAtom } from "../../state/auth";

type OnPublishButtonClicked = (instance: Instance) => void;
type InstancesTableProps = {
  instances: Instance[],
  onPublishButtonClicked: OnPublishButtonClicked

}
const InstancesTable: React.FC<InstancesTableProps> = ({instances, onPublishButtonClicked}) => {
  
  return <table className="table-fixed">
    <thead>
      <tr>
        <th className="px-4 py-2">ホスト</th>
        <th className="px-4 py-2">公開承認日</th>
        <th className="px-4 py-2">作成日</th>
        <th className="px-4 py-2">更新日</th>
        <th className="px-4 py-2">アクション</th>

      </tr>
    </thead>
    <tbody>
      {
        instances.map((i)=>{
          return <tr>
            <td className="border px-4 py-2">{i.host}</td>
            <td className="border px-4 py-2">{i.publishedAt == null ? "未配信" : dayjs(i.publishedAt).format("YYYY/M/DD")}</td>
            <td className="border px-4 py-2">{dayjs(i.createdAt).format("YYYY/M/DD")}</td>
            <td className="border px-4 py-2">{dayjs(i.updatedAt).format("YYYY/M/DD")}</td>
            <td className="border px-4 py-2">
              <button className="bg-sky-600 p-1 rounded-md text-white hover:bg-sky-500" onClick={() => onPublishButtonClicked(i)}>
                承認
              </button>
            </td>
          </tr>
        })
      }
    </tbody>
  </table>
}
const AllInstancesPage: React.FC = () => {
  
  const query = useQuery({queryKey: ['getInstances'], queryFn: instanceRepository.getInstances});
  const approveMutation = useMutation({
    mutationFn: instanceRepository.approve,
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['getInstances']
      })
    }
  })


  const onPublishButtonClicked: OnPublishButtonClicked = (instance) => {
    approveMutation.mutate(instance.id)
  }
  return <BodyLayout topAppBar={
    <AppBarLayout>
      全てのインスタンス
    </AppBarLayout>
  }>
    <div className="p-4">
      {
        query.isLoading
          ? "Loading"
          : query.isError
          ? "Error"
          : query.data
          ? <InstancesTable 
              instances={query.data}
              onPublishButtonClicked={onPublishButtonClicked}
            />
          : null
      }
      
    </div>
  </BodyLayout>
}
export default AllInstancesPage;