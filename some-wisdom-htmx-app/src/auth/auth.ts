import { AppError, Errors } from "../shared/errors";
import fs from "fs";
import path from "path";
import * as Files from "../shared/files";

//TODO: use files utils
export class AuthSessions {

    //TODO: clean up expired sessions
    private readonly sessionsDir: string;
    private readonly sessionDuration: number;
    private readonly refreshInterval: number;

    constructor(sessionsDir: string, sessionDuration: number, refreshInterval: number) {
        this.sessionsDir = sessionsDir;
        this.sessionDuration = sessionDuration;
        this.refreshInterval = refreshInterval;
    }

    async authenticate(session: string): Promise<AuthUser | null> {
        const sData = await this.sessionFromFile(session);
        if (!sData) {
            return null;
        }
        if (this.hasSessionExpired(sData, Date.now())) {
            return null;
        }
        return sData.user;
    }

    private async sessionFromFile(session: string): Promise<SessionData | null> {
        try {
            const file = await Files.textFileContent(this.sessionPath(session));
            return JSON.parse(file) as SessionData;
        } catch (e) {
            console.info("Problem while reading session data...", e);
            return null;
        }
    }

    private sessionPath(session: string): string {
        return path.join(this.sessionsDir, `${session}.json`);
    }

    async create(user: AuthUser): Promise<string> {
        const session = crypto.randomUUID();
        const now = Date.now();

        await this.writeSessionToFile(session, new SessionData(user, now, now));

        return session;
    }

    private writeSessionToFile(session: string, data: SessionData): Promise<void> {
        return Files.writeTextFileContent(this.sessionPath(session), JSON.stringify(data));
    }

    async refresh(session: string): Promise<void> {
        const now = Date.now();

        const sData = await this.validatedSession(session, now);

        return this.writeSessionToFile(session, this.refreshedSessionData(sData, now));
    }

    private refreshedSessionData(data: SessionData, refreshedAt: number): SessionData {
        return new SessionData(data.user, data.createdAt, refreshedAt);
    }

    private async validatedSession(session: string, now: number): Promise<SessionData> {
        const sData = await this.sessionFromFile(session);
        if (!sData) {
            throw AppError.ofSingleError(Errors.INVALID_SESSION);
        }

        if (this.hasSessionExpired(sData, now)) {
            throw AppError.ofSingleError(Errors.EXPIRED_SESSION);
        }
        return sData;
    }

    async shouldRefresh(session: string): Promise<boolean> {
        const now = Date.now();
        const sData = await this.validatedSession(session, now);
        return (now - sData.refreshedAt) >= this.refreshInterval;
    }

    private hasSessionExpired(session: SessionData, now: number) {
        return now > (session.refreshedAt + this.sessionDuration);
    }

    async delete(session: string): Promise<void> {
        return Files.deleteFile(this.sessionPath(session));
    }
}

class SessionData {
    constructor(readonly user: AuthUser,
        readonly createdAt: number,
        readonly refreshedAt: number) { }
}

export class AuthUser {
    constructor(readonly id: number, readonly name: string) { }
}