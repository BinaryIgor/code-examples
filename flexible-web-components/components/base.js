export const Components = {
    //TODO: more options, fuller support, simplify, test
    mappedAttributes(element, elementSuffix,
        { defaultAttributes = {},
            defaultClass = "",
            toAddAttributes = {},
            toAddClass = "",
            toSkipAttributes = [] } = {}) {

        let baseAttributes = baseAtrributesFromDefaults(defaultAttributes, defaultClass);

        let mappedAttributes = mappedAttributesWithDefaults(element, elementSuffix, baseAttributes, toSkipAttributes);

        mappedAttributes = mappedAttributesWithToAddValues(mappedAttributes, toAddAttributes, toAddClass);

        return Object.entries(mappedAttributes).map(e => `${e[0]}="${e[1]}"`).join("\n");
    },


    attributeValueOrDefault(element, attribute, defaultValue) {
        const value = element.getAttribute(attribute);
        return value ? value : defaultValue;
    }
};

function baseAtrributesFromDefaults(defaultAttributes, defaultClass) {
    if (defaultClass) {
        defaultAttributes["class"] = defaultClass;
    }

    return defaultAttributes;
}

function mappedAttributesWithDefaults(element, elementSuffix, defaultAttributes, toSkipAttributes) {
    const replaceSuffix = `-${elementSuffix}`;
    const addSuffix = `-${elementSuffix}-add`;

    const toMapAttributes = element.getAttributeNames()
        .filter(a => a.endsWith(replaceSuffix) || a.endsWith(addSuffix));

    const mappedAttributes = { ...defaultAttributes };

    toMapAttributes.forEach(a => {
        let targetKey;
        let add;
        if (a.endsWith(addSuffix)) {
            add = true;
            targetKey = a.replace(addSuffix, "");
        } else {
            add = false;
            targetKey = a.replace(replaceSuffix, "");
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
