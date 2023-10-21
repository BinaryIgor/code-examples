export class Item {
    constructor(readonly name: string, readonly value: string) { }

    toString(): string {
        return `${this.name}: ${this.value}`;
    }
}

export const items = [
    new Item("First item", "1"),
    new Item("Second item", "22")
];

export const toAddItems = [
    new Item("Third item", "33"),
    new Item("Fourth item", "X")
];

export function stringifyItems(items: Item[]): string[] {
    return items.map(i => i.toString());
}