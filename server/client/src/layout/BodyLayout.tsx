import { ReactNode } from "react";
type Props = {
  children?: ReactNode;
  topAppBar?: ReactNode;
};
const BodyLayout: React.FC<Props> = (
  { children, topAppBar }
) => {
  return <div>
    <div>
      {topAppBar}
    </div>
    <div>
      {children && <></>}
    </div>
  </div>
}

export default BodyLayout;