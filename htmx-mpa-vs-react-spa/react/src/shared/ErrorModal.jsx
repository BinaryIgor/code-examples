import { useEffect, useState } from "react";
import { eventBus } from "./event-bus";
import { Events } from "./events";
import { useTranslation } from 'react-i18next';

export default function ErrorModal() {
	const [error, setError] = useState("");
	const [hidden, setHidden] = useState(true);

	const { t } = useTranslation();

	useEffect(() => {
		const subscriber = eventBus.subscribe(Events.SHOW_ERROR_MODAL, data => {
			if (data.errorParams) {
				setError(t(`errors.${data.error}`, data.errorParams));
			} else {
				setError(t(`errors.${data.error}`));
			}
			setHidden(false);
		});
		return () => eventBus.unsubscribe(subscriber);
	}, []);

	return (
		<div className={"top-0 right-0 fixed w-full h-screen z-50 pt-32 bg-black/60" + `${hidden ? " hidden" : ""}`}>
			<div className="w-11/12 md:w-8/12 xl:w-2/5 px-8 pt-8 pb-12 m-auto relative rounded-lg bg-slate-200">
				<span className="text-4xl absolute top-0 right-2 hover:text-zinc-400 cursor-pointer"
					onClick={() => setHidden(true)}>&times;</span>
				<div className="text-2xl font-medium text-red-600">{t('errors.title')}</div>
				<div className="text-lg pt-4">{error}</div>
			</div>
		</div>);
}