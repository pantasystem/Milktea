import dayjs from "dayjs";
import React from "react";
import { useParams } from "react-router";
import { useInstanceDetailQuery } from "../../data/instances";
import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import { Instance } from "../../models/instance";

type InstanceDetailContentProps = {
  instance: Instance
}
const InstanceDetailContent: React.FC<InstanceDetailContentProps> = ({instance}) => {
  return <div className="p-4">
    <div className="pb-4">
      <div className="rounded-md drop-shadow-md bg-white p-4">
        <div className="text-2xl">
          {instance.host}
        </div>
      </div>
    </div>
    <div className="pb-4">
      <div className="rounded-md drop-shadow-md bg-white p-4">
        <div className="text-xl">
          基本情報
        </div>
        <div>
          Id: {instance.id}  
        </div>
        <div>
          Host: {instance.host}  
        </div>
        <div>
          作成日: {dayjs(instance.createdAt).format("YYYY/M/D")}
        </div>
        <div>
          更新日: {dayjs(instance.updatedAt).format("YYYY/M/D")}
        </div>
      </div>
    </div>
    
    <div className="pb-4">
      <div className="rounded-md drop-shadow-md bg-white p-4">
        <div className="text-xl">
          ステータス
        </div>
        <div>
          {instance.publishedAt 
            ? '公開済み' 
            : instance.deletedAt
            ? '削除済み'
            : '未承認'
          }
        </div>
      </div>
    </div>
    <div className="pb-4">
      <div className="rounded-md drop-shadow-md bg-white p-4">
        <div className="text-xl">
          メタ情報
        </div>
      </div>
    </div>
  </div>
  
}
const InstanceDetailPage: React.FC = () => {
  const { instanceId } = useParams();
  
  const query = useInstanceDetailQuery({instanceId: instanceId ?? ''});
  return <BodyLayout topAppBar={
    <AppBarLayout>
      インスタンス詳細
    </AppBarLayout>
  }>
    {
      query.isLoading ? 'Loading'
      : query.isError ? 'Error'
      : query.data ? <InstanceDetailContent instance={query.data}/>
      : null
    }
  </BodyLayout>
}

export default InstanceDetailPage;