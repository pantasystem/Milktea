import React from "react"
import { SubmitHandler, useForm } from "react-hook-form"
import { useUpdateInstanceClietMaxBodySize } from "../../data/instances"
import { Instance } from "../../models/instance"

type Props = {
  instance: Instance,
}

type Inputs = {
  size: string,
}
export const InstanceClientBodySizeForm : React.FC<Props> = ({instance}) => {
  const {register, handleSubmit, setValue} = useForm<Inputs>({
    defaultValues: {
      size: (instance.clientMaxBodyByteSize || 0).toString()
    }
  });

  const updateMutation = useUpdateInstanceClietMaxBodySize({instanceId: instance.id})

  const onSubmit: SubmitHandler<Inputs> = async (e) => {
    await updateMutation.mutateAsync({instanceId: instance.id, size: parseInt(e.size)});
  };
  return (
    <div className="pt-2">
      <form onSubmit={handleSubmit(onSubmit)}>
        <label htmlFor="input-body-size">バイト数</label>
        <input id="input-body-size" type="number" className="block w-9/12 border p-2 bg-gray-100 rounded-md" {...register("size")} />
        <div className="pt-2">
          {[100, 200, 300, 400, 500, 600, 700, 800, 900].map((n)=>
            <SetByteSizeButton onClick={() => setValue("size", (1048576 * n).toString())}>
            {n}MB
            </SetByteSizeButton>  
          )}
          <SetByteSizeButton onClick={() => setValue("size", (1048576 * 1000).toString())}>
            1GB
          </SetByteSizeButton>
        </div>
        

        <button type="submit" className="bg-sky-700 text-white p-2 rounded-lg mt-2">更新</button>

      </form>
      
    </div>
  )
}

const SetByteSizeButton = ({onClick, children}: {onClick: () => void, children: React.ReactNode}) => {
  return (
    <button type="button" onClick={onClick} className="bg-gray-500 text-white p-2 rounded-lg mr-1">
      {children}
    </button>
  )
}