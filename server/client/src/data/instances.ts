import { useMutation, useQuery } from "@tanstack/react-query";
import { queryClient } from "../App";
import { instanceRepository } from "../repositories/instance-repository";
export type FilterType = "all" | "approved" | "unapproved";

export const useInstancesQuery = ({filterType}: {filterType: FilterType}) => {
  return useQuery({queryKey: ['getInstances', filterType], queryFn: async () => {
      const res = await instanceRepository.getInstances()
    if (filterType === "all") {
      return res;
    } else if (filterType === "approved") {
      return res.filter((i) => i.publishedAt != null)
    } else if (filterType === "unapproved") {
      return res.filter((i) => i.publishedAt == null)
    }
  }});
}

export const useInstanceDetailQuery = ({instanceId}: {instanceId: string}) => {
  return useQuery({queryKey: ['getInstanceDetail', instanceId], queryFn: () => {
    return instanceRepository.get(instanceId);
  }});
}

export const useInstanceInfoQuery = ({host}: {host: string}) => {
  return useQuery({queryKey: ['getInstanceInfo', host], queryFn: () => {
    return instanceRepository.getInstanceInfo(host);
  }});
}

export const useUpdateInstanceClietMaxBodySize = ({instanceId}: {instanceId: string}) => {
  return useMutation({
    mutationFn: async ({instanceId, size}:{instanceId: string, size: number}) => {
      await instanceRepository.updateClientBodyByteSize(instanceId, {size: size})
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['getInstanceDetail', instanceId]);
    }
  })
}