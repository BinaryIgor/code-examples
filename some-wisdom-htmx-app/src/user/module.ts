import { Router, Request, Response } from "express";
import { InMemoryUserRepository } from "./repository";
import { Base64PasswordHasher } from "./password-hasher";
import { User, UserService } from "./domain";
import * as Web from "../shared/web";
import * as Views from "../shared/views";
import * as UserViews from "./views";
import { OptionalErrorCode } from "../shared/errors";
import { AuthSessions } from "../auth/auth";
import * as AuthWeb from "../auth/web";

const USER_PROFILE_ENDPOINT = "/user/profile";
const SIGN_IN_ENDPOINT = "/user/sign-in";
const SIGN_IN_EXECUTE_ENDPOINT = `${SIGN_IN_ENDPOINT}/execute`;
const SIGN_IN_VALIDATE_NAME_ENDPOINT = `${SIGN_IN_ENDPOINT}/validate-name`;
const SIGN_IN_VALIDATE_PASSWORD_ENDPOINT = `${SIGN_IN_ENDPOINT}/validate-password`;
const SIGN_OUT_ENDPOINT = "/user/sign-out";


export function build(authSessions: AuthSessions,
    sessionCookies: AuthWeb.SessionCookies,
    returnHomePage: (req: Request, res: Response, withSwappedNavigation: boolean) => void): UserModule {

    const userRepository = new InMemoryUserRepository();

    const passwordHasher = new Base64PasswordHasher();
    const userService = new UserService(userRepository, passwordHasher);

    const router = Router();

    router.get(SIGN_IN_ENDPOINT, (req: Request, res: Response) => {
        returnSignInPage(req, res, false);
    });

    function returnSignInPage(req: Request, res: Response, withNavigationToHide: boolean) {
        Web.returnHtml(res,
            UserViews.signInPage(SIGN_IN_EXECUTE_ENDPOINT, SIGN_IN_VALIDATE_NAME_ENDPOINT, SIGN_IN_VALIDATE_PASSWORD_ENDPOINT,
                withNavigationToHide, Web.shouldReturnFullPage(req)));
    }

    router.post(SIGN_IN_VALIDATE_NAME_ENDPOINT, (req: Request, res: Response) => {
        const { nameError, passwordError } = validateSignInInput(req);
        const inputValid = nameError == null && passwordError == null;

        Web.returnHtml(res,
            Views.inputErrorIf(nameError, UserViews.CLASSES.signInNameInputError),
            Views.formValidatedTrigger(UserViews.SIGN_FORM_LABEL, inputValid));
    });

    function validateSignInInput(req: Request): { nameError: OptionalErrorCode, passwordError: OptionalErrorCode } {
        const input = req.body as UserSignInInput;

        const nameError = userService.validateUserName(input.name);
        const passwordError = userService.validateUserPassword(input.password);

        return { nameError, passwordError };
    }

    router.post(SIGN_IN_VALIDATE_PASSWORD_ENDPOINT, (req: Request, res: Response) => {
        const { nameError, passwordError } = validateSignInInput(req);
        const inputValid = nameError == null && passwordError == null;

        Web.returnHtml(res,
            Views.inputErrorIf(passwordError, UserViews.CLASSES.signInPasswordInputError),
            Views.formValidatedTrigger(UserViews.SIGN_FORM_LABEL, inputValid));
    });

    router.post(SIGN_IN_EXECUTE_ENDPOINT, Web.asyncHandler(async (req: Request, res: Response) => {
        const input = req.body as UserSignInInput;
        const user = userService.signIn(input.name, input.password);

        const session = await authSessions.create(user);

        sessionCookies.setCookie(res, session);

        //For proper visibility of home page's navigation
        AuthWeb.setCurrentUser(req, user);

        returnHomePage(req, res, true);
    }));

    router.post(SIGN_OUT_ENDPOINT, Web.asyncHandler(async (req: Request, res: Response) => {
        const session = sessionCookies.sessionFromCookie(req);
        if (session) {
            await authSessions.delete(session);
            sessionCookies.setCookie(res, session, true);
            //For proper visibility of home page's navigation
            AuthWeb.setCurrentUser(req, null);
        }
        returnSignInPage(req, res, true);
    }));

    router.get(USER_PROFILE_ENDPOINT, (req: Request, res: Response) => {
        Web.returnHtml(res, UserViews.profilePage(AuthWeb.currentUserNameOrThrow(req), Web.shouldReturnFullPage(req)));
    });

    return new UserModule(router, SIGN_IN_ENDPOINT,
        {
            create(user: User): void {
                userRepository.create(user);
            },
            usersOfIds(ids: number[]): Map<number, User> {
                return userService.usersOfIds(ids);
            }
        });
}

class UserSignInInput {
    constructor(readonly name: string, readonly password: string) { }
}

export class UserModule {
    constructor(readonly router: Router,
        readonly signInEndpoint: string,
        readonly client: UserClient) { }
}

export interface UserClient {

    create(user: User): void

    usersOfIds(ids: number[]): Map<number, User>
}