<template>
  <div class="sso-page">
    <div class="sso-box">{{ message }}</div>
  </div>
</template>

<script>
import Api from '../apis/api'

export default {
  name: 'SsoLogin',
  data() {
    return {
      message: '正在进入控制台...'
    }
  },
  mounted() {
    const { ticket, redirect } = this.$route.query
    if (!ticket) {
      this.message = '登录凭证缺失'
      return
    }

    Api.user.ssoLogin({ ticket }, ({ data }) => {
      this.$store.commit('setToken', JSON.stringify(data.data))
      const target = typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/device-management'
      this.$router.replace(target)
    }, () => {
      this.message = '登录失败，请返回平台重试'
    })
  }
}
</script>

<style scoped>
.sso-page {
  align-items: center;
  background: #f8fafc;
  color: #0f172a;
  display: flex;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  height: 100vh;
  justify-content: center;
}

.sso-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  font-size: 14px;
  padding: 18px 22px;
}
</style>
