import {createApp} from 'vue'
// import './style.css'
import App from './App.vue'

// Toast
import 'vant/es/toast/style';
// Dialog
import 'vant/es/dialog/style';
// Notify
import 'vant/es/notify/style';
// ImagePreview
import 'vant/es/image-preview/style';
import router from "./config/route";



const app = createApp(App);
app.use(router)

app.mount('#app')
