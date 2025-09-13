<script lang="ts" setup>

// TODO: editable years if not defined
interface Props {
    years: number;
    assetOrCurrency1?: {
        marketSize: number;
        growthRate: number
    };
    assetOrCurrency2?: {
        marketSize: number;
        growthRate: number
    };
}

const { years, assetOrCurrency1, assetOrCurrency2 } = defineProps<Props>();

const marketSizeIncreasedByRate = (marketSize: number, growthRate: number) => {
    const increasedMarketSize = marketSize + (marketSize * growthRate / 100.0);
    if (increasedMarketSize <= 0 || Number.isNaN(increasedMarketSize)) {
        return undefined;
    }
    return increasedMarketSize;
};

const marketSizeIncreasedByRateInGivenYears = (marketSize: number, growthRate: number, years: number) => {
    let increasedMarketSize: number | undefined = marketSize;
    for (let i = 0; i < years; i++) {
        increasedMarketSize = marketSizeIncreasedByRate(increasedMarketSize, growthRate);
        if (!increasedMarketSize) {
            return undefined;
        }
    }
    return increasedMarketSize;
};

const nYearsPredicitionNumerator = (n: number) => {
    if (assetOrCurrency1) {
        return marketSizeIncreasedByRateInGivenYears(assetOrCurrency1.marketSize, assetOrCurrency1.growthRate, n);
    }
    return undefined;
}

const nYearsPredicitionDenominator = (n: number) => {
    if (assetOrCurrency2) {
        return marketSizeIncreasedByRateInGivenYears(assetOrCurrency2.marketSize, assetOrCurrency2.growthRate, n);
    }
    return undefined;
}

const nYearsPrediction = (n: number) => {
    const numerator = nYearsPredicitionNumerator(n);
    const denominator = nYearsPredicitionDenominator(n);
    if (numerator && denominator) {
        return Math.round(numerator * 1000 / denominator) / 1000.0;
    }
    return undefined;
};

const exponentialNumberString = (n?: number) => n?.toExponential(3) ?? '';

</script>

<template>
    <div class="underline">{{ exponentialNumberString(nYearsPredicitionNumerator(years)) ?? '' }} /
        {{ exponentialNumberString(nYearsPredicitionDenominator(years)) ?? '' }} = {{
            nYearsPrediction(years) }}</div>
</template>