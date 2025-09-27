<script lang="ts" setup>
import { ref, computed } from 'vue';
import ProjectionsResult from './ProjectionsResult.vue';

interface Props {
    assetOrCurrency1?: AssetOrCurrency
    assetOrCurrency2?: AssetOrCurrency
}

interface AssetOrCurrency {
    name: string;
    marketSize: number;
}

const { assetOrCurrency1, assetOrCurrency2 } = defineProps<Props>();

const assetOrCurrency1ExpectedGrowthRate = ref<number>();
const assetOrCurrency2ExpectedGrowthRate = ref<number>();
const projectionYears = ref<number>();

const assetOrCurrency1WithExpectedGrowthRate = computed<{ marketSize: number, growthRate: number } | undefined>(() => {
    if (assetOrCurrency1 && assetOrCurrency1ExpectedGrowthRate.value != undefined) {
        return { marketSize: assetOrCurrency1.marketSize, growthRate: assetOrCurrency1ExpectedGrowthRate.value };
    }
    return undefined;
});

const assetOrCurrency2WithExpectedGrowthRate = computed<{ marketSize: number, growthRate: number } | undefined>(() => {
    if (assetOrCurrency2 && assetOrCurrency2ExpectedGrowthRate.value != undefined) {
        return { marketSize: assetOrCurrency2.marketSize, growthRate: assetOrCurrency2ExpectedGrowthRate.value };
    }
    return undefined;
});

const nYearsProjectionYearString = computed<string>(() => {
    const currentYear = new Date().getFullYear();
    if (projectionYears.value) {
        return `${currentYear + projectionYears.value}`;
    }
    return `${currentYear} + N`;
});

</script>

<template>
    <h2 class="my-4 text-xl">Projections</h2>
    <div class="rounded border-1 p-2">
        <div>{{ assetOrCurrency1 ? assetOrCurrency1.name : "Asset/Currency 1" }} expected annual growth rate:</div>
        <input type="number" class="px-2 cursor-pointer" placeholder="%" v-model="assetOrCurrency1ExpectedGrowthRate">
        <div>{{ assetOrCurrency2 ? assetOrCurrency2.name : "Asset/Currency 2" }} expected annual growth rate:</div>
        <input type="number" class="px-2 cursor-pointer" placeholder="%" v-model="assetOrCurrency2ExpectedGrowthRate">
        <div v-for="y in [1, 5, 10]">
            <div class="mt-4">In {{ y }} years ({{ new Date().getFullYear() + y }}):</div>
            <ProjectionsResult v-if="assetOrCurrency1WithExpectedGrowthRate && assetOrCurrency2WithExpectedGrowthRate"
                :years="y" :assetOrCurrency1=assetOrCurrency1WithExpectedGrowthRate
                :assetOrCurrency2=assetOrCurrency2WithExpectedGrowthRate />
            <div v-else>-</div>
        </div>
        <div class="mt-4">In <input type="number" placeholder="N" class="max-w-[60px] px-2" v-model="projectionYears"> years ({{
            nYearsProjectionYearString }}):</div>
        <ProjectionsResult
            v-if="projectionYears && assetOrCurrency1WithExpectedGrowthRate && assetOrCurrency2WithExpectedGrowthRate"
            :years=projectionYears :assetOrCurrency1=assetOrCurrency1WithExpectedGrowthRate
            :assetOrCurrency2=assetOrCurrency2WithExpectedGrowthRate />
        <div v-else>-</div>
    </div>
</template>