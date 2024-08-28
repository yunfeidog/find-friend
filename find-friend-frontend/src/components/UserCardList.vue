<template>
    <van-card
        v-for="user in props.userList"
        :desc="user.profile"
        :title="`${user.username}（${user.ikunCode}）`"
        :thumb="user.avatarUrl"
    >
        <template #tags>
            <van-tag plain type="primary" v-for="tag in user.tags"
                     style="margin-right: 5px;margin-top: 5px;">
                {{ tag }}
            </van-tag>
        </template>
        <template #footer>
            <van-button size="mini" type="default">联系我</van-button>
        </template>
    </van-card>
</template>

<script setup lang="ts">
import {UserType} from "../models/user";

interface UsercardListProps {
    userList: UserType[]
}
const props = defineProps<UsercardListProps>()

</script>

<style scoped>

</style>
