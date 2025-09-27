<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Asset, Currency } from '../data/api';
import MarketsComparatorInput from './MarketsComparatorInput.vue';

interface Props {
    assets: Asset[];
    currencies: Currency[];
    onChosenFromMarketSizeChange: (assetOrCurrency: string, marketSize: number) => void;
    onChosenToMarketSizeChange: (assetOrCurrency: string, marketSize: number) => void;
}

const { assets, currencies,
    onChosenFromMarketSizeChange,
    onChosenToMarketSizeChange
} = defineProps<Props>();

console.log("Passed props: ", assets, currencies);

const fromMarketSize = ref<number>();
const toMarketSize = ref<number>();

const chosenMarketsComparedValue = computed<number>(() => {
    console.log("Chosen markets comparison...", fromMarketSize.value, toMarketSize.value);
    if (!fromMarketSize.value || !toMarketSize.value) {
        return 0;
    }
    return Math.round(fromMarketSize.value * 1000 / toMarketSize.value) / 1000.0;
});

const onChosenFromMarketSizeChangeHandler = (assetOrCurrency: string, marketSize: number) => {
    fromMarketSize.value = marketSize;
    onChosenFromMarketSizeChange(assetOrCurrency, marketSize);
};

const onChosenToMarketSizeChangeHandler = (assetOrCurrency: string, marketSize: number) => {
    toMarketSize.value = marketSize;
    onChosenToMarketSizeChange(assetOrCurrency, marketSize);
};

</script>

<template>
    <div class="rounded border-1 p-2">
        <markets-comparator-input-wc :assets="assets.map(a => ({ name: a.name, marketSize: a.marketSize }))"
            :currencies="currencies.map(c => ({ name: c.code.name, marketSize: c.marketSize }))" />
        <MarketsComparatorInput :assets="assets" :currencies="currencies"
            :onChosenMarketSizeChange="onChosenFromMarketSizeChangeHandler" />
        <div class="py-4">to</div>
        <MarketsComparatorInput :assets="assets" :currencies="currencies"
            :onChosenMarketSizeChange="onChosenToMarketSizeChangeHandler" />
        <div class="underline text-xl my-4">{{ fromMarketSize?.toExponential(3) }} / {{ toMarketSize?.toExponential(3)
        }} = {{ chosenMarketsComparedValue }}</div>
    </div>
</template>