const ANDROID_OS = "Android";
const IOS_OS = "iOS";
const MAC_OS = "Mac OS";
const LINUX_OS = "Linux";
const WINDOWS_OS = "Windows";
const UNKNOWN_OS = "Unknown";

export function getOS() {
    try {
        const userAgent = navigator.userAgent.toLowerCase();
        if (userAgent.includes("android")) {
            return ANDROID_OS;
        }
        if (userAgent.includes("linux")) {
            return LINUX_OS;
        }

        return UNKNOWN_OS;
    } catch (e) {
        return UNKNOWN_OS;
    }
}