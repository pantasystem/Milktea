import { useMutation, useQuery } from "@tanstack/react-query";
import { queryClient } from "../../../App";
import { instanceRepository } from "../../../repositories/instance-repository";
import InstancesTable, { OnPublishButtonClicked } from "./instances-table";

type FilterType = "all" | "approved" | "unapproved";
type Props = {
  filterType: FilterType
}
const InstancesStatePage: React.FC<Props> = ({filterType}) => {
  
  const query = useQuery({queryKey: ['getInstances'], queryFn: async () => {
    const res = await instanceRepository.getInstances()
    if (filterType === "all") {
      return res;
    } else if (filterType === "approved") {
      return res.filter((i) => i.publishedAt != null)
    } else if (filterType === "unapproved") {
      return res.filter((i) => i.publishedAt == null)
    }
  }});
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