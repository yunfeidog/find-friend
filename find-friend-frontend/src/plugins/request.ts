import axios from "axios";
import {useRoute, useRouter} from "vue-router";
import {showToast} from "vant";

const request = axios.create({
    baseURL: "http://localhost:8080/api"
});

request.defaults.withCredentials = true; // 允许跨域请求携带cookie
request.interceptors.request.use(function (config) {
    console.log("发送请求")
    return config;
}, function (error) {
    return Promise.reject(error);
});

request.interceptors.response.use(function (response) {
    console.log("响应请求")
    if (response.data.code === 40101) {
        // 未登录
        showToast("请先登录");
        window.location.href = "/#/user/login";
    }
    return response.data;
}, function (error) {
    return Promise.reject(error);
});
export default request;
