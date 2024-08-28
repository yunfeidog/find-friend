<template>
    <van-form @submit="onSubmit">
        <van-field
            v-model="editUser.currentValue"
            :name="editUser.editKey"
            :label="editUser.editName"
            :placeholder="`请输入${editUser.editName}`"
        />
        <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
                提交
            </van-button>
        </div>
    </van-form>

</template>

<script setup>

import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import request from "../../plugins/request";
import {showFailToast, showToast} from "vant";
import {getCurrentUser} from "../../api/user";

const route = useRoute();
const router = useRouter();
const editUser = ref({
    editKey: route.query.editKey,
    editName: route.query.editName,
    currentValue: route.query.currentValue
})
console.log(route.query)


const currentUser = getCurrentUser()

const onSubmit = async (values) => {
    const currentUser = await getCurrentUser()
    if (!currentUser) {
        showFailToast('请先登录')
        await router.push('/login')
        return
    }
    console.log("当前用户")
    console.log(currentUser)
    const res = await request.post('/user/update', {
        'id': currentUser.id,
        [editUser.value.editKey]: editUser.value.currentValue
    })
    if (res.code === 0 ) {
        showToast('修改成功')
        router.back()
    } else {
        showToast('修改失败')
    }
}

</script>

<style scoped>

</style>
