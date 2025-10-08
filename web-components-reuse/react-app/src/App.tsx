import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import Home from "./Home";

export default function App() {
  const { t } = useTranslation();

  useEffect(() => {
    document.addEventListener('events.show-error-modal', e => {
      const { error } = (e as CustomEvent).detail;
      document.dispatchEvent(new CustomEvent('em.show', {
        detail: {
          title: t('errors.title'),
          error: t('errors.' + error)
        }
      }));
    });
  }, []);

  return (
    <>
      <Home />
      <error-modal></error-modal>
    </>
  )
}
