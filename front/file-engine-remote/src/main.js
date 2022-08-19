import {
    createApp
} from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import VueLoading from 'vue-loading-overlay';
import App from './App.vue'

import './assets/main.css'
import 'vue-loading-overlay/dist/vue-loading.css';

const app = createApp(App)
app.use(VueLoading)
app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.mount('#app')