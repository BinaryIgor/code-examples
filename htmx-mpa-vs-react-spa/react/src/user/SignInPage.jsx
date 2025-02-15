import { useTranslation } from "react-i18next";
import { useRef } from "react";
import { useNavigate } from "react-router";
import { api } from "../shared/api";
import { Events } from "../shared/events";
import * as Validator from "../shared/validator";
import InputWithError from "../shared/InputWithError";

export default function SignInPage() {
	const { t } = useTranslation();

	const emailInput = useRef();
	const passwordInput = useRef();
	const navigate = useNavigate();

	const signIn = async (e) => {
		e.preventDefault();
		const response = await api.post("sign-in", {
			email: emailInput.current.value,
			password: passwordInput.current.value
		});
		Events.showErrorModalOrRun(response, () => {
			Events.userSignedIn();
			navigate("/");
		});
	};
	return (<>
		<title>{t('signInPage.title')}</title>
		<h1 className="text-2xl my-8">{t('signInPage.title')}</h1>
		<form onSubmit={signIn}>
			<InputWithError
				type="email" name="email"
				placeholder={t('signInPage.emailPlaceholder')}
				inputRef={emailInput}
				isValid={isEmailValid}
				errorMessage={t('errors.UserEmailValidationException')}>
			</InputWithError>
			<InputWithError
				addInputClass="mt-4"
				type="password" name="password"
				placeholder={t('signInPage.passwordPlaceholder')}
				inputRef={passwordInput}
				isValid={isPasswordValid}
				errorMessage={t('errors.UserPasswordValidationException')}>
			</InputWithError>
			<input className="button-like px-8 py-2 mt-4" type="submit" value={t('signInPage.signIn')}></input>
		</form>
	</>);
}

function isEmailValid(email) {
	return Validator.hasLength(email, 3, 100) &&
		email.indexOf('@') >= 2 &&
		email.includes('.') && email.indexOf('.') < (email.length - 1);
}

function isPasswordValid(password) {
	return Validator.hasLength(password, 8, 50);
}