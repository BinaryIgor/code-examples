<script setup lang="ts">
import { ref, computed, useTemplateRef, onMounted, onUnmounted } from 'vue';
import type { Asset, Currency } from '../../data/api';

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

const assetInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
    assets.map(a => ({ name: a.name, marketSize: a.marketSize })));

const currencyInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
    currencies.map(c => ({ name: c.code.name, marketSize: c.marketSize })));

console.log("Passed props: ", assets, currencies);

const fromMarketSize = ref<number>();
const toMarketSize = ref<number>();

const chosenMarketsComparedValue = computed<number>(() => {
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


const marketsComparatorInputFrom = useTemplateRef<HTMLElement>("input-from");
console.log("Input from:", marketsComparatorInputFrom);

const fromInputId = crypto.randomUUID();
const toInputId = crypto.randomUUID();

const chosenMarketSizeChangedEventHandler = (e: Event) => {
    const { componentId, name, marketSize } = (e as CustomEvent).detail as { componentId: string, name: string, marketSize: number };
    if (componentId == fromInputId) {
        onChosenFromMarketSizeChangeHandler(name, marketSize);
    } else if (componentId == toInputId) {
        onChosenToMarketSizeChangeHandler(name, marketSize);
    }
};

onMounted(() => {
    console.log("From...", marketsComparatorInputFrom);
    // (marketsComparatorInputFrom.value as any).onChosenMarketSizeChange = onChosenFromMarketSizeChangeHandler;
    document.addEventListener('mci:chosen-market-size-changed', chosenMarketSizeChangedEventHandler);
});

onUnmounted(() => {
    document.removeEventListener('mci:chosen-market-size-changed', chosenMarketSizeChangedEventHandler);
});

</script>

<template>
    <div class="rounded border-1 p-2">
        <markets-comparator-input ref="input-from" :id=fromInputId :assets="assetInputOptions"
            :currencies="currencyInputOptions" />
        <div class="py-4">to</div>
        <markets-comparator-input ref="input-to" :id=toInputId :assets="assetInputOptions"
            :currencies="currencyInputOptions" />
        <div class="underline text-xl my-4">{{ fromMarketSize?.toExponential(3) }} / {{ toMarketSize?.toExponential(3)
            }} = {{ chosenMarketsComparedValue }}</div>
    </div>
</template>