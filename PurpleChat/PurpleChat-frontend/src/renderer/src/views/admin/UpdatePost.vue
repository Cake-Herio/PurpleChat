<template>
    <Dialog :title="dialogConfig.title" :buttons="dialogConfig.buttons" :show="dialogConfig.show"
        @close="dialogConfig.show = false" :width="'600px'">

        <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="100pxw" @submit.prevent>
            <el-form-item label="版本号">
                {{ formData.version }}
            </el-form-item>

            <el-form-item label="发布状态" prop="status">
                <el-radio-group v-model="formData.status">
                    <el-radio :value="0">取消发布</el-radio>
                    <el-radio :value="1">灰度发布</el-radio>
                    <el-radio :value="2">全网发布</el-radio>
                </el-radio-group>
            </el-form-item>

            <el-form-item label="灰度UID" prop="grayscaleUid" v-if="formData.status == 1">
                <div class="tag-panel">
                    <el-tag v-for="(tag, index) in formData.grayscaleUid" :key="tag" closable @close="closeTag(index)"
                        :type="tag.type" class="tag">
                        {{ tag }}
                    </el-tag>

                    <div class="tag input" v-if="showInput">
                        <el-input size="small" clearable placeholder="请输入UID" v-model.trim="tagInput"
                            @blur="addDeviceId" @keyup.enter="addDeviceId" maxlength="12">
                        </el-input>
                    </div>

                    <div class="tag" v-if="!showInput">
                        <el-button size="small" type="primary" @click="showInputHandler">添加</el-button>
                    </div>
                </div>
            </el-form-item>

        </el-form>
    </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const dialogConfig = reactive({
    show: false,
    title: "发布更新",
    buttons: [
        {
            type: "danger",
            text: "确定",
            click: (e) => {
                submitForm();
            },
        },
    ],
});

const formData = ref({
    status: null,  // 初始状态设置为 null 或适当的默认值
    grayscaleUid: []  // 初始化为空数组
});
const formDataRef = ref(null)
const rules = {
    status: [
        { required: true, message: "请选择发布状态", trigger: "blur" },
    ],
    grayscaleUid: [
        {
            required: true,
            message: '请输入至少一个账号',
            trigger: 'blur',
            validator: (rule, value, callback) => {
                if (formData.value.status == 1 && (!value || value.length === 0)) {
                    callback(new Error('请输入至少一个账号'));
                } else {
                    for (let uid of value) {
                        if (!/^[GU]\d{11}$/.test(uid)) {
                            callback(new Error('账号格式不正确'));
                            return;
                        }
                    }
                    callback();
                }
            }
        }
    ],
}

const emit = defineEmits(["reload"])
const submitForm = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {};
        Object.assign(params, formData.value);
        let result = await proxy.Request({
            url: proxy.Api.postUpdate,
            params,
        });
        if (!result) {
            return;
        }
        proxy.Message.success('操作成功')
        emit('reload')
        dialogConfig.show = false
    });

}

const showEdit = (data) => {
    dialogConfig.show = true
    nextTick(() => {
        formDataRef.value.resetFields()
        formData.value = Object.assign({
            id: data.id,
            version: data.version,
            status: data.status,
            grayscaleUid: data.grayscaleUid ? data.grayscaleUid.split(',') : []
        }, data)
    })
}

defineExpose({
    showEdit
})

const showInput = ref(false)
const tagInput = ref()
const addDeviceId = () => {
    if (tagInput.value) {
        // 如果 grayscaleUid 不是数组，初始化为数组
        if (!Array.isArray(formData.value.grayscaleUid)) {
            formData.value.grayscaleUid = [];
        }
        formData.value.grayscaleUid.push(tagInput.value);
        tagInput.value = ''; // 清空输入框
        showInput.value = false;
    }
}

const showInputHandler = () => {
    showInput.value = true
}

const closeTag = (index) => {
    formData.value.grayscaleUid.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.tag-panel {
    display: flex;
    flex-wrap: wrap;
    align-items: center;

    .tag {
        margin: 0px 5px 5px 0px
    }

    .input {
        width: 150px;
    }

}
</style>
