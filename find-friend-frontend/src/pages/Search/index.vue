<template>
    <form action="/">
        <van-search
            v-model="searchText"
            show-action
            placeholder="请输入搜索关键词"
            @search="onSearch"
            @cancel="onCancel"
        />
    </form>


    <van-divider content-position="left">已选标签</van-divider>

    <template v-if="activeIds.length===0">请选择标签</template>

    <van-row gutter="16">
        <van-col v-for="tag in activeIds">
            <van-tag closeable size="small" type="primary" @close="doClose(tag)">
                {{ tag }}
            </van-tag>
        </van-col>
    </van-row>

    <van-divider content-position="left">选择标签</van-divider>

    <van-tree-select
        v-model:active-id="activeIds"
        v-model:main-active-index="activeIndex"
        :items="filterTagList"
    />

    <van-button type="primary"  block @click="doSearchResult">搜索</van-button>


</template>

<script setup lang="ts">
import {ref} from "vue";
import {showToast} from "vant";
import {useRouter} from "vue-router";

//搜索框文字
const searchText = ref('');
//已选中标签
const activeIds = ref([]);
//当前激活的标签
const activeIndex = ref(0);
const tagList = [
    {
        text: '性别',
        children: [
            {text: '男', id: '男'},
            {text: '女', id: '女'},
        ],
    },
    {
        text: '年级',
        children: [
            {text: '大一', id: '大一'},
            {text: '大二', id: '大二'},
        ],
    },
]
const filterTagList = ref(tagList);

const router = useRouter();
//搜索 过滤
const onSearch = () => {
    filterTagList.value = tagList.map(parentTag => {
        const tempChildren = [...parentTag.children];
        const tempParentTag = {...parentTag};
        tempParentTag.children = tempChildren.filter(childTag => {
            return childTag.text.includes(searchText.value);
        });
        return tempParentTag;
    });

}

//清空搜索框
const onCancel = () => {
    showToast('取消');
    searchText.value = '';
    filterTagList.value = tagList;
}

//关闭标签
const doClose = (tag) => {
    activeIds.value = activeIds.value.filter(item => {
        return item !== tag;
    });
}

const doSearchResult = () => {
    router.push({
        path: '/user/list',
        query: {
            tags: activeIds.value
        }
    })
}

</script>

<style scoped>

</style>
