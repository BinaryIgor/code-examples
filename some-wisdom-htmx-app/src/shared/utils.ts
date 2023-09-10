export function randomNumber(min: number, max: number): number {
    return min + Math.round(Math.random() * max);
}