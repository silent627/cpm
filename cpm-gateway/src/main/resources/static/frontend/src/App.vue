<template>
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  min-height: 100vh;
}

/* 确保输入框宽度不受清除按钮影响 */
.el-input {
  width: 100%;
}

.el-input__wrapper {
  width: 100%;
  box-sizing: border-box;
}

/* 清除按钮是绝对定位的，不会影响输入框宽度 */
.el-input__suffix {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
}
</style>

