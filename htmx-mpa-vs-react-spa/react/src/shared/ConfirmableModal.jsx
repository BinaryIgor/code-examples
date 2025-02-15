import { useEffect, useRef, useState } from "react";
import { eventBus } from "./event-bus";
import { Events } from "./events";
import { classesWithClassIf } from "./ReactUtils";

export default function ConfirmableModal({ modalId = "",
  title = "",
  content = "",
  left = "",
  right = "",
  onLeft, onRight,
  hideOnOutsideClick = true } = {}) {

  const [hidden, setHidden] = useState(true);
  const containerRef = useRef();

  const showModal = () => {
    document.body.classList.add("overflow-y-hidden");
    setHidden(false);
  };

  const hideModal = () => {
    document.body.classList.remove("overflow-y-hidden");
    setHidden(true);
  };

  useEffect(() => {
    const subscriber = eventBus.subscribe(Events.SHOW_CONFIRMABLE_MODAL, id => {
      if (modalId == id) {
        showModal();
      }
    });
    return () => {
      eventBus.unsubscribe(subscriber);
    };
  }, []);

  return (
    <div id={modalId}
      className={classesWithClassIf("top-0 right-0 fixed w-full h-screen z-50 pt-32 bg-black/60", "hidden", hidden)}
      ref={containerRef}
      onClick={e => {
        if (hideOnOutsideClick && e.target == containerRef.current) {
          hideModal();
        }
      }}>
      <div className="w-11/12 md:w-8/12 xl:w-2/5 px-8 pt-8 pb-12 m-auto relative rounded-lg bg-slate-200">
        <span className="text-4xl absolute top-0 right-2 hover:text-zinc-400 cursor-pointer"
          onClick={hideModal}>&times;</span>
        <div className="text-2xl font-medium">{title}</div>
        <div className="text-lg pt-4 pb-4">{content}</div>
        <span className="absolute bottom-0 left-0 p-4 ml-4 cursor-pointer text-xl"
          onClick={() => {
            if (onLeft) {
              onLeft();
            }
            hideModal();
          }}>{left}</span>
        <span className="absolute bottom-0 right-0 p-4 mr-4 cursor-pointer text-xl"
          onClick={() => {
            if (onRight) {
              onRight();
            }
            hideModal();
          }}>{right}</span>
      </div>
    </div>
  );
}