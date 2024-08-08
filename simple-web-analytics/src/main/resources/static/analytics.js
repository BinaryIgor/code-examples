const ANDROID_OS_REGEX = /(android)(.*)/i;
const IOS_OS_REGEX = /(ipad|iphone|ipod)(.*)/i;
const WINDOWS_OS_REGEX = /(windows)(.*)/i;
const MAC_OS_REGEX = /(mac)(.*)/i;
const LINUX_OS_REGEX = /(linux)(.*)/i;

const ANDROID_OS = "Android";
const IOS_OS = "iOS";
const MAC_OS = "Mac OS";
const LINUX_OS = "Linux";
const WINDOWS_OS = "Windows";
const UNKNOWN_OS = "Unknown";

const REGEXES_AND_OSES = [
    [ANDROID_OS_REGEX, ANDROID_OS],
    [IOS_OS_REGEX, IOS_OS],
    [WINDOWS_OS_REGEX, WINDOWS_OS],
    [MAC_OS_REGEX, MAC_OS],
    [LINUX_OS_REGEX, LINUX_OS]
];

const OPERA_BROWSER_REGEX = /(opr|opera)(.*)/i
const EDGE_BROWSER_REGEX = /(edg|edge)(.*)/i;
const FIREFOX_BROWSER_REGEX = /(firefox|fxios)(.*)/i;
const SAFARI_BROWSER_REGEX = /(safari)(.*)/i;
const CHROME_BROWSER_REGEX = /(chrome|chromium)(.*)/i;

const SAFARI_BROWSER = "Safari";
const OPERA_BROWSER = "Opera";
const FIREFOX_BROWSER = "Firefox";
const EDGE_BROWSER = "Edge";
const CHROME_BROWSER = "Chrome";
const UNKNOWN_BROWSER = "Unknown";

// Order matters here!
// Opera and Edge browsers also contain Chrome keyword
// Chrome browsers contain Safari keyword
const REGEXES_AND_BROWSERS = [
    [OPERA_BROWSER_REGEX, OPERA_BROWSER],
    [EDGE_BROWSER_REGEX, EDGE_BROWSER],
    [FIREFOX_BROWSER_REGEX, FIREFOX_BROWSER],
    [CHROME_BROWSER_REGEX, CHROME_BROWSER],
    [SAFARI_BROWSER_REGEX, SAFARI_BROWSER]
];

const DESKTOP_DEVICE = "Desktop";
const TABLET_DEVICE = "Tablet";
const MOBILE_DEVICE = "Mobile";
const UNKNOWN_DEVICE = "Unknown";

const DESKTOP_DEVICE_WIDTH_THRESHOLD = 1200;
const MOBILE_DEVICE_THRESHOLD = 500;

const DEVICE_ID_KEY = "device-id";

let sessionPlatform = null;
let sessionBrowser = null;

export function getPlatform() {
    try {
        if (sessionPlatform) {
            return sessionPlatform;
        }

        for (const [regex, os] of REGEXES_AND_OSES) {
            sessionPlatform = tryToMatchPlatform(regex, os);
            if (sessionPlatform) {
                return sessionPlatform;
            }
        }

        return UNKNOWN_OS;
    } catch (e) {
        return UNKNOWN_OS;
    }
}

function tryToMatchPlatform(regex, os) {
    const match = navigator.userAgent.match(regex);
    if (match) {
        const osVersion = getOsVersion(match[2]);
        if (osVersion) {
            return `${os} ${osVersion}`;
        }
        return os;
    }
    return undefined;
}

function getOsVersion(versionToParse) {
    try {
        return versionToParse.split(")")[0].trim();
    } catch (e) {
        return undefined;
    }
}

export function getBrowser() {
    try {
        if (sessionBrowser) {
            return sessionBrowser;
        }

        for (const [regex, browser] of REGEXES_AND_BROWSERS) {
            sessionBrowser = tryToMatchBrowser(regex, browser);
            if (sessionBrowser) {
                return sessionBrowser;
            }
        }

        return UNKNOWN_BROWSER;
    } catch (e) {
        return UNKNOWN_BROWSER;
    }
}

function tryToMatchBrowser(regex, browser) {
    const match = navigator.userAgent.match(regex);
    if (match) {
        const browserVersion = getBrowserVersion(match[2]);
        if (browserVersion) {
            return `${browser} ${browserVersion}`;
        }
        return browser;
    }
    return undefined;
}

function getBrowserVersion(versionToParse) {
    try {
        return versionToParse.replace("/", "").split(" ")[0];
    } catch (e) {
        return undefined;
    }
}

export function getDevice() {
    const portraitMode = window.innerWidth < window.innerHeight;
    const landscapeMode = window.innerWidth >= window.innerHeight;

    if ((portraitMode && window.innerWidth < MOBILE_DEVICE_THRESHOLD) ||
        (landscapeMode && window.innerHeight < MOBILE_DEVICE_THRESHOLD)) {
        return MOBILE_DEVICE;
    }

    if (window.innerWidth < DESKTOP_DEVICE_WIDTH_THRESHOLD) {
        return TABLET_DEVICE;
    }

    if (window.innerWidth >= DESKTOP_DEVICE_WIDTH_THRESHOLD) {
        return DESKTOP_DEVICE;
    }

    return UNKNOWN_DEVICE;
}

export function getDeviceId() {
    let deviceId = localStorage.getItem(DEVICE_ID_KEY);
    if (!deviceId) {
        deviceId = crypto.randomUUID();
        localStorage.setItem(DEVICE_ID_KEY, deviceId);
    }
    return deviceId;

}

export async function sendEvent(targetUrl, type, data = null) {
    try {
        const event = JSON.stringify({
            url: document.location.href,
            browser: getBrowser(),
            platform: getPlatform(),
            device: getDevice(),
            type: type,
            data: data
        });

        console.log("About to send event...", event);

        const response = await fetch(targetUrl,
            {
                method: "POST",
                body: event,
                headers: {
                    "content-type": "application/json",
                    "device-id": getDeviceId()
                }
            });

        // Sending analytical events is not critical, so we don't want to crush application because of it
        if (!response.ok) {
            console.error(`Failed to send ${type} event to ${targetUrl} url:`, data);
        }
    } catch (e) {
        console.error(`Failed to send ${type} event to ${targetUrl} url:`, e);
    }
}