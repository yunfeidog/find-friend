<template>
    <template v-if="user">
        <van-cell title="当前用户 " :value="user?.username"/>
        <van-cell title="修改信息 " is-link to="/user/update"/>
        <van-cell title="我创建的队伍" is-link to="/user/team/create"/>
        <van-cell title="我加入的队伍" is-link to="/user/team/join"/>

    </template>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import request from "../../plugins/request";
import {showToast} from "vant";
import {getCurrentUser} from "../../api/user";

// const user = {
//     id: 1,
//     username: 'ikun',
//     userAccount: 'ikun',
//     avatarUrl: "https://s2.loli.net/2023/10/16/QRiUYmDLB2vZuE6.webp",
//     gender: '男',
//     phone: "114514",
//     email: "1@qq.com",
//     ikunCode: 1,
//     createTime: new Date()
// }

const router = useRouter();

const toEdit = (editKey: string, editName: string, currentValue: string) => {
    router.push({
        path: '/user/edit',
        query: {
            editKey,
            editName,
            currentValue,
        }
    })
}
const user = ref()
onMounted(async () => {
    user.value = await getCurrentUser()
})

</script>

<style scoped>

</style>
