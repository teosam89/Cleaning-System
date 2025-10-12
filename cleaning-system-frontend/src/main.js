import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
// import axios from "axios";
// import Vue from "vue";
//
// axios.defaults.baseURL = "http://localhost:8080/api"; // 后端 API 地址
// Vue.prototype.$axios = axios;

const app = createApp(App);

// Register Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(router).use(ElementPlus).mount("#app");
