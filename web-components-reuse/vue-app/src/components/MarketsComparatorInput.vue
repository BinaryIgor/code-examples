<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { Asset, Currency } from '../data/api';

interface Props {
    assets: Asset[];
    currencies: Currency[];

    onChosenMarketSizeChange: (assetOrCurrency: string, marketSize: number) => void;
}

const { assets, currencies, onChosenMarketSizeChange } = defineProps<Props>();

const assetOrCurrencyInput = ref<string>();
const curencyTurnoverInputMultiplier = ref<number>(1);

const assetsOrCurrencyOptions = computed<string[]>(() => {
    const options: string[] = [];
    assets.forEach(a => options.push(a.name));
    currencies.forEach(c => options.push(c.code.name));
    return options;
});

const assetOrCurrencyClickHandler = (ac: string) => {
    assetOrCurrencyInput.value = ac;
    calculateChosenMarketSizeChange();
};

const calculateChosenMarketSizeChange = () => {
    if (!assetOrCurrencyInput.value) {
        return;
    }

    const assetInput = assets.find(a => a.name == assetOrCurrencyInput.value);
    const currencyInput = currencies.find(c => c.code.name == assetOrCurrencyInput.value);

    const inputMarketSize = assetInput ? assetInput.marketSize : currencyInput!!.marketSize * curencyTurnoverInputMultiplier.value;

    console.log("Input asset or currency...", assetInput, currencyInput);
    console.log("Market size...", inputMarketSize);

    onChosenMarketSizeChange(assetOrCurrencyInput.value, inputMarketSize);
};

const isAsset = (assetOrCurrency?: string): boolean => assetOrCurrency ? assets.find(a => a.name == assetOrCurrency) != undefined : false;
const isCurrency = (assetOrCurrency?: string): boolean => assetOrCurrency ? currencies.find(c => c.code.name == assetOrCurrency) != undefined : false;

watch(curencyTurnoverInputMultiplier, () => calculateChosenMarketSizeChange());
watch(() => assets, () => {
    calculateChosenMarketSizeChange();
});
watch(() => currencies, () => {
    calculateChosenMarketSizeChange();
})

</script>

<template>
    <drop-down-container>
        <div>
            <span data-drop-down-anchor>{{ assetOrCurrencyInput ?? 'Asset/Currency' }}</span>
            <span v-if="isAsset(assetOrCurrencyInput)" class="mx-2">market size</span>
            <span v-else-if="isCurrency(assetOrCurrencyInput)">
                <input class="mx-2 px-2 max-w-[75px]" v-model="curencyTurnoverInputMultiplier">
                <span>days turnover</span>
            </span>
        </div>
        <ul class="border-1 rounded cursor-pointer bg-white max-h-[350px] overflow-y-auto overflow-x-none"
            data-drop-down-options>
            <li class="p-2 border-b-1 last:border-0" v-for="ac in assetsOrCurrencyOptions"
                @click="() => assetOrCurrencyClickHandler(ac)">
                {{ ac }}</li>
        </ul>
    </drop-down-container>
</template>