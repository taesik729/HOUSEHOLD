<template>
  <RouterView />
</template>

<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { Capacitor } from '@capacitor/core'

// AdMob 설정 (서버값으로 ON/OFF 제어 가능하도록 분리)
const ADMOB_CONFIG = {
  appId:    'ca-app-pub-4592571865246292~3538251911',
  bannerId: 'ca-app-pub-4592571865246292/8487662975',
  showAds:  false,  // ← 여기서 광고 ON/OFF 제어 (true로 바꾸면 광고 노출)
}

onMounted(async () => {
  // 네이티브 앱(Android/iOS)에서만 AdMob 초기화
  if (!Capacitor.isNativePlatform()) return

  if (!ADMOB_CONFIG.showAds) return

  try {
    const { AdMob, BannerAdSize, BannerAdPosition } = await import('@capacitor-community/admob')
    await AdMob.initialize({ requestTrackingAuthorization: false })
    await AdMob.showBanner({
      adId:     ADMOB_CONFIG.bannerId,
      adSize:   BannerAdSize.BANNER,
      position: BannerAdPosition.BOTTOM_CENTER,
      margin:   0,
    })
  } catch (e) {
    console.warn('AdMob error:', e)
  }
})
</script>
