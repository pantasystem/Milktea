import { useMutation } from "@tanstack/react-query";
import { queryClient } from "../../../App";
import { instanceRepository } from "../../../repositories/instance-repository";
import InstancesTable, { OnPublishButtonClicked } from "./instances-table";
import React from "react";
import { FilterType, useInstancesQuery } from "../../../data/instances";


type Props = {
  filterType: FilterType
}
const InstancesStatePage: React.FC<Props> = ({filterType}) => {
  
  const query  = useInstancesQuery({filterType})

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

  return <div className="p-4">
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
}
export default InstancesStatePage