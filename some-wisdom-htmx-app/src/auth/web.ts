import  { Response, Request } from "express";
import { cookieValue } from "../shared/web";
import { AuthUser } from "./auth";
import { AppError, Errors } from "../shared/errors";

export class SessionCookies {

    constructor(private readonly sessionDuration: number,
        private readonly sessionCookieKey: string,
        private readonly httpsOnly: boolean) {}

    setCookie(res: Response, session: string, expired: boolean = false) {
        res.setHeader('Set-Cookie', this.cookie(session, expired));
    }

    cookie(session: string, expired: boolean = false): string {
        const expiresAt = new Date();
        if (expired) {
            expiresAt.setTime(0);
        } else {
            expiresAt.setTime(expiresAt.getTime() + this.sessionDuration);
        }

        let cookie = `${this.sessionCookieKey}=${session}; HttpOnly; SameSite=Strict; Path=/; Expires=${expiresAt.toUTCString()}`;
        if (this.httpsOnly) {
            cookie = cookie + `; Secure`;
        }

        return cookie;
    }

    sessionFromCookie(req: Request): string | null {
        return cookieValue(req, this.sessionCookieKey);
    }
}

export function setCurrentUser(req: any, user: AuthUser | null) {
    req.user = user;
}

export function currentUser(req: any): AuthUser | null {
    return req.user as AuthUser;
}

export function currentUserName(req: any): string | null {
    return currentUser(req)?.name ?? null;
}

export function currentUserOrThrow(req: any): AuthUser {
    const user = currentUser(req);
    if (!user) {
        throw AppError.ofSingleError(Errors.NOT_AUTHENTICATED);
    }
    return user;
}

export function currentUserNameOrThrow(req: any): string {
    return currentUserOrThrow(req).name;
}