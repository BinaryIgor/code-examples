import { UserRepository, User } from "./domain";

export class InMemoryUserRepository implements UserRepository {

    private readonly db = new Map<number, User>();

    ofId(id: number): User | null {
        return this.db.get(id) ?? null;
    }

    ofIds(ids: number[]): Map<number, User> {
        const found = new Map<number, User>();
        ids.forEach(id => {
            const user = this.ofId(id);
            if (user) {
                found.set(id, user);
            }
        });
        return found;
    }

    ofName(name: string): User | null {
        for (let u of this.db.values()) {
            if (u.name == name) {
                return u;
            }
        }
        return null;
    }

    create(user: User): void {
        this.db.set(user.id, user);
    }    
}