import { useEffect } from "react";
import { api } from "../shared/api";
import { Events } from "../shared/events";
import { eventBus } from "../shared/event-bus";
import { useTranslation } from "react-i18next";
import { useUser } from "../shared/UserContext";
import { Link } from "react-router";

export default function AccountPage() {
  const { user } = useUser();
  const { t } = useTranslation();

  useEffect(() => {
    eventBus.publish({ type: Events.REFRESH_USER_DATA });
  }, []);

  if (user.loading) {
    return null;
  }

  const signOut = async () => {
    const response = await api.post("/sign-out");
    Events.showErroModalIfFailure(response);
    Events.userSignedOut();
  };

  const { name, email, language } = user.data;

  return (<>
    <title>{t('accountPage.title')}</title>
    <h1 className="text-2xl my-8">{t('accountPage.title')}</h1>
    <div className="my-8">
      <p>{t('accountPage.name')}: {name}</p>
      <p>{t('accountPage.email')}: {email}</p>
      <p>{t('accountPage.language')}: {language}</p>
    </div>
    <Link className="underline" to="/sign-in" onClick={signOut}>{t('accountPage.signOut')}</Link>
  </>);
}