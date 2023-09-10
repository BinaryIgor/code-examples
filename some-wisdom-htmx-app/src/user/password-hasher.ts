import { PasswordHasher } from "./domain";

export class Base64PasswordHasher implements PasswordHasher {
    verify(rawPassword: string, hashedPassword: string): boolean {
        return Buffer.from(rawPassword, 'utf8').toString('base64') == hashedPassword;
    }
}
