import React from "react";
import { SubmitHandler, useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";
import { instanceRepository } from "../../repositories/instance-repository";

type Inputs = {
  host: string,
  isPublish: boolean,
}

export const InstanceRegisterPage: React.FC = () => {

  const navigator = useNavigate();
  const {
    register,
    handleSubmit,
  } = useForm<Inputs>();

  const onSubmit: SubmitHandler<Inputs> = async (e) => {
    await instanceRepository.create(e); 
    navigator(-1);
  }

  return (
    <BodyLayout topAppBar={
      <AppBarLayout>インスタンスを登録</AppBarLayout>
    }>
      <div className="p-4">
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="pt-2 pb-2">
            <label htmlFor="host">Host</label>
            <div className="flex items-center">  
              https://<input id="host" type="text" className="block w-9/12 border p-2 bg-gray-100 rounded-md" {...register("host")} />
            </div>
          </div>
          <div className="pt-2 pb-2">
            <label htmlFor="isPublish">即時公開</label>
            <input type="checkbox" id="isPublish" {...register("isPublish")}/>
          </div>
          <button type="submit" className="bg-sky-700 text-white p-2 rounded-lg">送信</button>
        </form>
      </div>
    </BodyLayout>
  )
}