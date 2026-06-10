<template>
  <RouterView />
</template>

<script setup>
import { RouterView, useRouter } from 'vue-router'
import { onMounted } from 'vue'
import { Capacitor } from '@capacitor/core'

// AdMob 설정 (서버값으로 ON/OFF 제어 가능하도록 분리)
const ADMOB_CONFIG = {
  appId:    'ca-app-pub-4592571865246292~3538251911',
  bannerId: 'ca-app-pub-4592571865246292/8487662975',
  showAds:  false,  // ← 여기서 광고 ON/OFF 제어 (true로 바꾸면 광고 노출)
}

const router = useRouter()

onMounted(async () => {
  // 딥링크 URL 처리 (비밀번호 재설정 등)
  if (Capacitor.isNativePlatform()) {
    const { App: CapApp } = await import('@capacitor/app')
    CapApp.addListener('appUrlOpen', (data) => {
      try {
        const url = new URL(data.url)
        const search = url.search
        const hash   = url.hash
        router.push('/reset-password' + search + hash)
      } catch (e) {}
    })
  }

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
