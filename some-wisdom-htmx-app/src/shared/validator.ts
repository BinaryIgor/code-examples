export function hasAnyContent(str: string): boolean {
    return str as any && str.trim().length > 0;
}

export function hasLength(str: string, min: number, max: number): boolean {
    return str as any && str.length >= min && str.length <= max;
}