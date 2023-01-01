import dayjs from "dayjs";
import React from "react";
import { useParams } from "react-router";
import { useInstanceDetailQuery, useInstanceInfoQuery } from "../../data/instances";
import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import ScrollLayout from "../../layout/scroll-layout";
import { Instance } from "../../models/instance";
import { InstanceClientBodySizeForm } from "./instance-client-max-body-size-form";

type InstanceDetailContentProps = {
  instance: Instance
}

const InstanceDetailContentNormal : React.FC<InstanceDetailContentProps> = ({instance}) => {
  return (
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
  )
}

const InstanceMetaContent: React.FC<{host: string}> = ({host}) => {
  const query = useInstanceInfoQuery({host: host});
  if (query.isLoading) {
    return <div>Loading</div>
  } else if (query.isError) {
    return <div>
      取得失敗:{`${query.error}`}
    </div>
  } else if (query.data) {
    return <div>
      
      <div className="flex">
        {query.data.iconUrl && <img className="w-6 h-6 rounded-md" src={query.data.iconUrl} alt="" />}{query.data.host}
      </div>
      <div>
        Description: {query.data.description}
      </div>
    </div>
  } else {
    return <></>
  }
}
const InstanceDetailContent: React.FC<InstanceDetailContentProps> = ({instance}) => {
  return <div className="p-4">
    <div className="pb-4">
      <div className="rounded-md drop-shadow-md bg-white p-4">
        <div className="flex items-center justify-between">
          <div className="text-2xl">
            {instance.host}
          </div>
          <div>
            <a href={`https://${instance.host}`} target="blank" className="bg-sky-600 text-white p-2 rounded-md hover:bg-sky-500">表示</a>
          </div>
        </div>
        
      </div>
    </div>
    <div className="pb-4">
      <InstanceDetailContentNormal instance={instance}/>
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
          送信可能なボディのサイズ(バイト)
        </div>
        <div>
          {instance.clientMaxBodyByteSize?.toString() || "未設定"}
          <InstanceClientBodySizeForm instance={instance} />
        </div>
      </div>
    </div>
    <div className="pb-4">
      <div className="rounded-md drop-shadow-md bg-white p-4">
        <div className="text-xl">
          メタ情報
        </div>
        <InstanceMetaContent host={instance.host} />
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
    <ScrollLayout>
    {
      query.isLoading ? 'Loading'
      : query.isError ? 'Error'
      : query.data ? <InstanceDetailContent instance={query.data}/>
      : null
    }
    </ScrollLayout>
  </BodyLayout>
}

export default InstanceDetailPage;