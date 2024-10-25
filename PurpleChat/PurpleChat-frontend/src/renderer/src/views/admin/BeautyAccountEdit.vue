<template>
    <Dialog :title="dialogConfig.title" :buttons="dialogConfig.buttons" :show="dialogConfig.show"
        @close="dialogConfig.show = false" :width="'400px'">
        <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="60px" @submit.prevent>
            <!--input输入-->
            <el-form-item label="邮箱" prop="email">
                <el-input clearable placeholder="请输入邮箱" v-model.trim="formData.email" :maxlength="50"></el-input>
            </el-form-item>

            <el-form-item label="靓号" prop="userId">
                <el-input clearable placeholder="请输入靓号" v-model.trim="formData.userId" :maxlength="12"></el-input>
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

const dialogConfig = ref({
    show: false,
    title: "编辑靓号",
    buttons: [
        {
            type: "primary",
            text: "确定",
            click: (e) => {
                submitForm();
            },
        },
    ],
});

const formData = ref({
    updateDescList: [],
})
const formDataRef = ref(null)
const rules = {
    email: [
        { required: true, message: "请输入邮箱", trigger: "blur" },
        { type: "email", message: "邮箱格式不正确", trigger: "blur" },
    ],
    userId: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        {
            pattern: /^[GU]\d{11}$/,
            message: '账号格式不正确',
            trigger: 'blur'
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
            url: proxy.Api.saveBeautyAccount,
            params,
        });
        if (!result) {
            return;
        }
        emit("reload");
        dialogConfig.value.show = false;
    });
    
}


const showEdit = (data = {}) => {
    dialogConfig.value.show = true;
    nextTick(() => {
        formDataRef.value.resetFields();
        // formData.value = Object.assign({}, data);
    })

}


defineExpose({
    showEdit
})

</script>

<style lang="scss" scoped></style>
