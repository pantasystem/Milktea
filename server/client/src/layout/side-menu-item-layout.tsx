import { ReactNode } from "react";
import { Link, To } from "react-router-dom";

type Props = {
  children?: ReactNode;
  to: To

}
const SideMenuItemLayout: React.FC<Props> = ({ children, to }) => {
  return (
    <Link to={to}>
    <div className="pt-1 pb-1 text-lg hover:bg-sky-600 pl-4 pr-4">
      {children}
    </div>
    </Link>
  )
};

export default SideMenuItemLayout;