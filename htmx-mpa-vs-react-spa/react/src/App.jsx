import ErrorModal from "./shared/ErrorModal";
import Navigation from "./shared/Navigation";

export default function App({ children }) {
  return (<>
    <ErrorModal />
    <div className="flex w-full min-h-screen">
      <Navigation />
      <div className="w-full p-4">
        {children}
      </div>
    </div>
  </>);
}