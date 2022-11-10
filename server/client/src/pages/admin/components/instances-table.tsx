import dayjs from "dayjs";
import { Instance } from "../../../models/instance";
import React from "react";
import { Link } from "react-router-dom";

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
        <th className="px-4 py-2">リクエストボディ許容サイズ(Byte)</th>
        <th className="px-4 py-2">アクション</th>

      </tr>
    </thead>
    <tbody>
      {
        instances.map((i)=>{
          return <tr>
            <td className="border px-4 py-2"><Link to={`/admin/instances/${i.id}`}>{i.host}</Link></td>
            <td className="border px-4 py-2">{i.publishedAt == null ? "未配信" : dayjs(i.publishedAt).format("YYYY/M/DD")}</td>
            <td className="border px-4 py-2">{dayjs(i.createdAt).format("YYYY/M/DD")}</td>
            <td className="border px-4 py-2">{dayjs(i.updatedAt).format("YYYY/M/DD")}</td>
            <td className="border px-4 py-2">{i.clientMaxBodyByteSize}</td>
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
export type {OnPublishButtonClicked};
export default InstancesTable;