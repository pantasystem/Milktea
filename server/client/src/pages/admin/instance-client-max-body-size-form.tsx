import { SubmitHandler, useForm } from "react-hook-form"
import { Instance } from "../../models/instance"
import { instanceRepository } from "../../repositories/instance-repository"

type Props = {
  instance: Instance,
}

type Inputs = {
  size: string,
}
export const InstanceClientBodySizeForm : React.FC<Props> = ({instance}) => {
  const {register, handleSubmit} = useForm<Inputs>({
    defaultValues: {
      size: (instance.clientMaxBodyByteSize || 0).toString()
    }
  });

  const onSubmit: SubmitHandler<Inputs> = async (e) => {
    console.log("onsubmi")
    await instanceRepository.updateClientBodyByteSize(instance.id, {size: parseInt(e.size)});
  };
  return (
    <div className="pt-2">
      <form onSubmit={handleSubmit(onSubmit)}>
        <label htmlFor="input-body-size">バイト数</label>
        <input id="input-body-size" type="number" className="block w-9/12 border p-2 bg-gray-100 rounded-md" {...register("size")} />
        <button type="submit" className="bg-sky-700 text-white p-2 rounded-lg mt-2">更新</button>
      </form>
      
    </div>
  )
}
  