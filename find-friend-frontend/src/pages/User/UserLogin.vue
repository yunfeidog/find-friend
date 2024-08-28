<template>
    <van-form @submit="onSubmit">
        <van-cell-group inset>
            <van-field
                v-model="userAccount"
                name="userAccount"
                label="用户账号"
                placeholder="请输入账号"
                :rules="[{ required: true, message: '请填写账号' }]"
            />
            <van-field
                v-model="userPassword"
                type="password"
                name="userPassword"
                label="密码"
                placeholder="密码"
                :rules="[{ required: true, message: '请填写密码' }]"
            />
        </van-cell-group>
        <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
                提交
            </van-button>
        </div>
    </van-form>

</template>

<script setup lang="ts">

import {useRouter} from "vue-router";
import {ref} from "vue";
import request from "../../plugins/request";
import {showToast} from "vant";

const router = useRouter();

const userAccount = ref('');
const userPassword = ref('');
const onSubmit = async (values) => {
    console.log('submit', values);
    const res = await request.post('/user/login', {
        userAccount: userAccount.value,
        userPassword: userPassword.value
    })
    console.log(res, "用户登录")
    if (res.code === 0) {
        showToast('登录成功')
        await router.replace('/user')
    } else {
        showToast('登录失败')
    }
};

</script>

<style scoped>

</style>
