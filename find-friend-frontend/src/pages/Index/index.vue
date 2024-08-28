<template>
    <user-card-list :user-list="userList"/>
    <van-empty v-if="!userList ||userList.length<1" description="数据为空"/>


</template>

<script setup>
import {useRoute} from "vue-router";
import {onMounted, ref} from "vue";
import request from "../../plugins/request";
import qs from 'qs'
import UserCardList from "../../components/UserCardList.vue";

const route = useRoute();
const {tags} = route.query;
const mockUser = {
    id: 1,
    username: 'ikun',
    userAccount: 'ikun',
    avatarUrl: "https://s2.loli.net/2023/10/16/QRiUYmDLB2vZuE6.webp",
    gender: '男',
    phone: "114514",
    email: "1@qq.com",
    ikunCode: 1,
    tags: ['java', 'python'],
    createTime: new Date(),
    profile: "蔡徐坤,会唱跳打篮球"
}
const userList = ref([])

onMounted(async () => {
    const userListData = await request.get('/user/recommend', {
        params: {
            pageSize: 50,
            pageNum: 1
        },
        paramsSerializer: params => {
            return qs.stringify(params, {indices: false})
        }
    }).then(function (response) {
        return response.data.records;
    }).catch(function (error) {
    })
    if (userListData) {
        userListData.forEach(user => {
            if (user.tags) {
                user.tags = JSON.parse(user.tags);
            }
        })
        userList.value = userListData;
    }
})

</script>

<style scoped>

</style>
