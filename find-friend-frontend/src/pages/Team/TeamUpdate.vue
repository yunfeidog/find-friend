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

import {onMounted, ref} from "vue";
import request from "../../plugins/request";
import {showFailToast, showSuccessToast, showToast} from "vant";
import {useRoute, useRouter} from "vue-router";
import {TeamType} from "../../models/team";

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
const addTeamData = ref < TeamType > ({})
const route = useRoute();
const id = route.query.id;
//获取队伍信息
onMounted(async () => {
    if (id <= 0) {
        showFailToast('获取队伍信息失败')
    }
    const res:any = await request.get("/team/get", {
        params: {
            id: id
        }
    })
    if (res.code === 0) {
        addTeamData.value = res.data;
    } else {
        showFailToast('获取队伍信息失败')
    }
})

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
    const res:any = await request.post("/team/update", postData);
    if (res?.code === 0 && res.data) {
        showSuccessToast('更新成功');
        router.push({
            path: '/team',
            replace: true,
        });
    } else {
        showFailToast('更新失败');
    }
}
</script>

<style scoped>

</style>
