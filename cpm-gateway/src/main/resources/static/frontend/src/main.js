import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import dialogDragPlugin from './plugins/dialogDrag'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用对话框拖拽插件（全局自动为所有对话框添加拖拽和缩放功能）
app.use(dialogDragPlugin)

app.use(ElementPlus)
app.use(router)
app.mount('#app')

