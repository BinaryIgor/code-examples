export function formatMoney(value, denomination) {
    const zeros = value.length;
    if (zeros > 15) {
        return `${value.substring(0, zeros - 15)} ${value.substring(zeros - 15, zeros - 12)} ${value.substring(zeros - 12, zeros - 9)} ${value.substring(zeros - 9, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    if (zeros > 12) {
        return `${value.substring(0, zeros - 12)} ${value.substring(zeros - 12, zeros - 9)} ${value.substring(zeros - 9, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    if (zeros > 9) {
        return `${value.substring(0, zeros - 9)} ${value.substring(zeros - 9, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    if (zeros > 6) {
        return `${value.substring(0, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    return `${value} ${denomination}`;
}

// TODO
export const translations = {};