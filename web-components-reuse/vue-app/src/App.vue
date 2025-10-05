<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
// TODO: router not needed!

const { t } = useI18n();

onMounted(() => {
  document.addEventListener('events.show-error-modal', e => {
    const { error } = (e as CustomEvent).detail;
    document.dispatchEvent(new CustomEvent('em.show', {
      detail: {
        title: t('errors.title'),
        error: t('errors.' + error)
      }
    }));
  });
});

</script>

<template>
  <RouterView />
  <error-modal></error-modal>
</template>

<style scoped></style>
