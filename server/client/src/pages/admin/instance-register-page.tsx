import React from "react";
import AppBarLayout from "../../layout/app-bar-layout";
import BodyLayout from "../../layout/body-layout";

export const InstanceRegisterPage: React.FC = () => {
  return (
    <BodyLayout topAppBar={
      <AppBarLayout>インスタンスを登録</AppBarLayout>
    }>
      <div className="p-4">
        <form>
          
        </form>
      </div>
    </BodyLayout>
  )
}