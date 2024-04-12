export const Components = {
    mappedAttributes(element, elementId,
        { defaultAttributes = {},
            defaultClass = "",
            toAddAttributes = {},
            toAddClass = "",
            toSkipAttributes = [],
            keepId = false } = {}) {

        let baseAttributes = baseAtrributesFromDefaults(defaultAttributes, defaultClass);

        let mappedAttributes = mappedAttributesWithDefaults(element, elementId, baseAttributes, toSkipAttributes, keepId);

        mappedAttributes = mappedAttributesWithToAddValues(mappedAttributes, toAddAttributes, toAddClass);

        return Object.entries(mappedAttributes).map(e => `${e[0]}="${e[1]}"`).join("\n");
    },

    attributeValueOrDefault(element, attribute, defaultValue = "") {
        const value = element.getAttribute(attribute);
        return value ? value : defaultValue;
    },

    queryByCustomId(element, value) {
        return element.querySelector(`[data-custom-id="${value}"]`);
    },

    renderedCustomIdAttribute(value) {
        return `data-custom-id="${value}"`;
    }
};

function baseAtrributesFromDefaults(defaultAttributes, defaultClass) {
    if (defaultClass) {
        defaultAttributes["class"] = defaultClass;
    }

    return defaultAttributes;
}

function mappedAttributesWithDefaults(element, elementId, defaultAttributes, toSkipAttributes, keepId) {
    const replacePrefix = `${elementId}:`;
    const addPrefix = `${elementId}:add:`;

    const toMapAttributes = element.getAttributeNames()
        .filter(a => a.startsWith(replacePrefix) || a.startsWith(addPrefix));

    const mappedAttributes = { ...defaultAttributes };

    toMapAttributes.forEach(a => {
        let targetKey;
        let add;
        if (a.startsWith(addPrefix)) {
            add = true;
            targetKey = keepId ? a : a.replace(addPrefix, "");
        } else {
            add = false;
            targetKey = keepId ? a : a.replace(replacePrefix, "");
        }

        if (toSkipAttributes.includes(targetKey)) {
            return "";
        }

        if (add) {
            const prevValue = mappedAttributes[targetKey];
            const newValue = element.getAttribute(a);
            if (prevValue) {
                mappedAttributes[targetKey] = `${prevValue} ${newValue}`;
            } else {
                mappedAttributes[targetKey] = newValue;
            }
        } else {
            mappedAttributes[targetKey] = element.getAttribute(a);
        }
    });

    return mappedAttributes;
}

function mappedAttributesWithToAddValues(mappedAttributes, toAddAttributes, toAddClass) {
    if (toAddClass) {
        toAddAttributes["class"] = toAddClass;
    }

    for (const [key, value] of Object.entries(toAddAttributes)) {
        const prevValue = mappedAttributes[key];

        let newValue;
        if (prevValue) {
            newValue = `${value} ${prevValue}`;
        } else {
            newValue = value;
        }

        mappedAttributes[key] = newValue;
    }

    return mappedAttributes;
}