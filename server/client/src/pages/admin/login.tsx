import AppBarLayout from "../../layout/AppBarLayout";
import BodyLayout from "../../layout/BodyLayout";

const LoginForm: React.FC = () => {
  return <div className="w-full h-full  flex justify-center items-center">
    <div className="p-8 drop-shadow-lg bg-white rounded-lg">
      <div className="pb-4">
        <h1 className="text-xl">Login</h1>
      </div>
      <form>
        <div className="pt-2 pb-2">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" className="block w-96 border p-2 bg-gray-100 rounded-md"/>
        </div>
        <div className="pt-2 pb-2">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" className="block w-96 border p-2 bg-gray-100 rounded-md"/>
        </div>
        <button type="submit" className="bg-sky-700 text-white p-2 rounded-lg">Login</button>
      </form>
    </div>
    
  </div>

}
const LoginPage: React.FC = () => {
    return (
        <BodyLayout topAppBar={
            <AppBarLayout>
                Login
            </AppBarLayout>
        }>
            <LoginForm />
        </BodyLayout>
    )
}



export default LoginPage;