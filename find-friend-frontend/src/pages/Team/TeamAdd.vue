<template>
    <van-form @submit="onSubmit">
        <van-cell-group inset>
            <van-field
                v-model="addTeamData.name"
                name="name"
                label="队伍名"
                placeholder="请输入队伍名"
                :rules="[{ required: true, message: '请填写用户名' }]"
            />
            <van-field
                v-model="addTeamData.description"
                rows="4"
                autosize
                label="队伍描述"
                type="textarea"
                placeholder="请输入队伍描述"
            />
            <van-field
                is-link
                readonly
                name="datePicker"
                label="过期时间"
                :placeholder="addTeamData.expireTime??'请选择过期时间'"
                @click="showPicker = true"
            />
            <van-popup v-model:show="showPicker" position="bottom">
                <van-date-picker
                    :min-date="minDate"
                    @confirm="onConfirm"
                    @cancel="showPicker = false"/>
            </van-popup>
            <van-field name="stepper" label="步进器">
                <template #input>
                    <van-stepper v-model="addTeamData.maxNum"/>
                </template>
            </van-field>
            <van-field name="radio" label="队伍状态">
                <template #input>
                    <van-radio-group v-model="addTeamData.status" direction="horizontal">
                        <van-radio name="0">公开</van-radio>
                        <van-radio name="1">私有</van-radio>
                        <van-radio name="2">加密</van-radio>
                    </van-radio-group>
                </template>
            </van-field>
            <van-field v-model="addTeamData.password"
                       type="password"
                       name="password"
                       label="密码"
                       placeholder="请输入密码"
                       v-if="addTeamData.status==='2'"
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

import {ref} from "vue";
import request from "../../plugins/request";
import {showFailToast, showSuccessToast, showToast} from "vant";
import {useRouter} from "vue-router";

const result = ref('');
const showPicker = ref(false);
const router = useRouter();

const minDate = new Date();

const initFormData = {
    "description": "",
    "expireTime": null,
    "maxNum": 3,
    "name": "",
    "password": "",
    "status": 0,
}
//用户填写的表单数据
const addTeamData = ref({...initFormData})
const onConfirm = ({selectedValues}) => {
    result.value = selectedValues.join('-');
    console.log(result.value);
    const dateValue = new Date(result.value);
    addTeamData.value.expireTime = dateValue;
    showPicker.value = false;
};

// 提交
const onSubmit = async () => {
    const postData = {
        ...addTeamData.value,
        status: Number(addTeamData.value.status)
    }
    // todo 前端参数校验
    const res: any = await request.post("/team/add", postData);
    if (res?.code === 0 && res.data) {
        showSuccessToast('添加成功');
        router.push({
            path: '/team',
            replace: true,
        });
    } else {
        showFailToast('添加失败');
    }
}
</script>

<style scoped>

</style>
