import fs from "fs";
import path from "path";
import bodyParser from "body-parser";
import express, { NextFunction, Request, Response } from "express";
import * as FilesDb from "./files-db";
import * as Views from "./shared/views";
import { AppError, ErrorCode, Errors } from "./shared/errors";
import { AuthSessions, AuthUser } from "./auth/auth";
import * as Web from "./shared/web";
import { setCurrentUser, SessionCookies, currentUserName } from "./auth/web";
import * as AuthorsModule from "./authors/module";
import * as UserModule from "./user/module";
import * as QuotesModule from "./quotes/module";
import { getAppConfig } from "./app-config";

const appConfig = getAppConfig();

const sessionConfig = appConfig.session;

if (!fs.existsSync(sessionConfig.dir)) {
    fs.mkdirSync(sessionConfig.dir);
}

const authSessions = new AuthSessions(sessionConfig.dir, sessionConfig.dir, sessionConfig.refreshInterval);
const sessionCookies = new SessionCookies(sessionConfig.duration, "session-id", false);

const quoteNotesDbPath = path.join(appConfig.db.path, "__quote-notes.json");

const authorsModule = AuthorsModule.build(QuotesModule.quoteEndpoint);
const userModule = UserModule.build(authSessions, sessionCookies, authorsModule.returnHomePage);
const quotesModule = QuotesModule.build(quoteNotesDbPath,
    authorsModule.client.quoteOfId, userModule.client.usersOfIds);


if (appConfig.profile == "e2e-tests") {
    console.log(`e2e-tests profile, loading data from ${appConfig.db.path} path...`);

    staticFileContentOfPath(path.join(appConfig.db.path, "authors.json"))
        .then(db => FilesDb.importAuthors(db, authorsModule.client))
        .catch(e => console.log("Failed to load authors db!", e));

    staticFileContentOfPath(path.join(appConfig.db.path, "users.json"))
        .then(db => FilesDb.importUsers(db, userModule.client))
        .catch(e => console.log("Failed to load users db!", e));
}

const app = express();

app.use(bodyParser.urlencoded({ extended: true }));

// Weak etags are added by default, we don't want that
app.set('etag', false);

app.use(Web.asyncHandler(async (req: Request, res: Response, next: NextFunction) => {
    const session = sessionCookies.sessionFromCookie(req);
    let user: AuthUser | null;

    if (session) {
        user = await authSessions.authenticate(session);
    } else {
        user = null;
    }

    if (user) {
        setCurrentUser(req, user);
    }

    if (isPublicRequest(req) || user) {
        if (user && session && await authSessions.shouldRefresh(session)) {
            await authSessions.refresh(session);
            sessionCookies.setCookie(res, session);
        }
        next();
    } else {
        res.redirect(userModule.signInEndpoint);
    }
}));

function isPublicRequest(req: Request): boolean {
    return req.path.startsWith("/user") ||
        req.path.includes(".css") || req.path.includes(".js") || req.path.includes(".ico");
}

app.use(userModule.router);
app.use(authorsModule.router);
app.use(quotesModule.router);

app.get("/", (req: Request, res: Response) => authorsModule.returnHomePage(req, res, false));
app.get("/index.html", (req: Request, res: Response) => authorsModule.returnHomePage(req, res, false));

app.get("*", async (req: Request, res: Response) => {
    if (req.url.includes(".css")) {
        const fileContent = await staticFileContentOfPath(path.join(appConfig.assets.stylesPath, Web.fileNameFromPath(req)));
        Web.returnCss(res, fileContent, appConfig.assets.cacheControl);
    } else if (req.url.includes(".js")) {
        const fileContent = await staticFileContentOfPath(path.join(appConfig.assets.path, Web.fileNameFromPath(req)));
        Web.returnJs(res, fileContent, appConfig.assets.cacheControl);
    } else {
        Web.returnNotFound(res);
    }
})

function staticFileContentOfPath(path: string): Promise<string> {
    return fs.promises.readFile(path, 'utf-8');
}

app.use((error: any, req: Request, res: Response, next: NextFunction) => {
    console.error("Something went wrong...", error);
    //TODO: refactor!
    let status: number;
    let errors: ErrorCode[]
    if (error instanceof AppError) {
        status = appErrorStatus(error);
        errors = error.errors;
    } else {
        status = 500;
        //TODO: maybe more details
        errors = ["UNKOWN_ERROR"];
    }
    res.status(status);
    Web.returnHtml(res, Views.errorPage(errors, Web.shouldReturnFullPage(req), currentUserName(req)));
});

function appErrorStatus(error: AppError): number {
    for (let e of error.errors) {
        if (e == Errors.NOT_AUTHENTICATED || e == Errors.INVALID_SESSION || e == Errors.EXPIRED_SESSION) {
            return 401;
        }
        if (e == Errors.INCORRECT_USER_PASSWORD) {
            return 403;
        }
        if (e.includes("NOT_FOUND")) {
            return 404;
        }
    }
    return 400;
}

app.listen(appConfig.server.port, () => {
    console.log(`Server started on ${appConfig.server.port}`);
});

//TODO: graceful shutdown
process.on('SIGTERM', () => {
    console.log("Received SIGTERM signal, exiting...")
    process.exit();
});

process.on('SIGINT', () => {
    console.log("Received SIGINT signal, exiting...")
    process.exit();
});