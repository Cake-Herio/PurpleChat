<template>
    <Dialog :title="dialogConfig.title" :buttons="dialogConfig.buttons" :show="dialogConfig.show"
        @close="dialogConfig.show = false" :width="'400px'">
        <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="100px">
            <el-form-item label="版本号" prop="version">
                <el-input :maxLength="10" v-model="formData.version" placeholder="eg:1.0.0" />
            </el-form-item>

            <el-form-item label="文件类型" prop="fileType">
                <el-radio-group v-model="formData.fileType">
                    <el-radio :value="0">本地文件</el-radio>
                    <el-radio :value="1">外链</el-radio>
                </el-radio-group>
            </el-form-item>

            <el-form-item label="文件" prop="fileName" class="file-select" v-if="formData.fileType == 0">
                <div class="file-name">{{ formData.fileName }}</div>
                <el-upload name="file" :show-file-list="false" accept=".exe" :multiple="false"
                    :http-request="selectFile">
                    <el-button type="primary" size="small">选择文件</el-button>
                </el-upload>
            </el-form-item>

            <el-form-item label="外链地址" prop="outerLink" v-if="formData.fileType == 1">
                <el-input :maxLength="200" v-model="formData.outerLink" placeholder="请输入完整的外链地址" />
            </el-form-item>

            <el-form-item label="更新内容" class="update-form-item">
                <div class="update-desc-item" v-for="(item, index) in formData.updateDescList" :key="item">
                    <el-form-item :props="'updateList.' + index + '.title'"
                        :rules="{ required: true, message: '更新内容不能为空' }">
                        <div class="update-desc">
                            <div class="num">{{ index + 1 }}.</div>
                            <div class="input">
                                <el-input clearable placeholder="请输入更新内容" v-model="item.title"
                                    :maxlength="20"></el-input>
                            </div>
                            <div class="iconfont icon-add" v-if="index == 0" @click="addLine"></div>
                            <div class="iconfont icon-min" v-if="index > 0" @click="delLine(index)"></div>
                        </div>
                    </el-form-item>
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

const dialogConfig = ref({
    show: false,
    title: "发布更新",
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
    updateDescList: [{}],
})
const formDataRef = ref(null)
const rules = {
    version: [
        { required: true, message: "请输入版本号", trigger: "blur" },
        {
            validator: (rule, value, callback) => {
                if (!/^\d+\.\d+\.\d+$/.test(value)) {
                    callback(new Error('版本号格式不正确'));
                } else {
                    callback();
                }
            }, trigger: "blur"
        }
    ],
    fileType: [
        { required: true, message: "请选择文件类型", trigger: "blur" },
    ],
    fileName: [
        { required: true, message: "请选择文件", trigger: "blur" },
    ],
    outerLink: [
        { required: true, message: "请输入外链地址", trigger: "blur" },
    ],
    updateType: [
        { required: true, message: "请选择更新类型", trigger: "blur" },
    ]
}

const selectFile = (file) => {
    file = file.file
    formData.value.fileName = file.name;
    formData.value.file = file;

}

const addLine = () => {
    formData.value.updateDescList.push({ title: "" });
}

const delLine = (index) => {
    formData.value.updateDescList.splice(index, 1);
}

const emit = defineEmits(["reload"])
const submitForm = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {};
        Object.assign(params, formData.value);
        //格式化更新内容
        const updateDescList = formData.value.updateDescList.map(item => item.title);
        params.updateDesc = updateDescList.join("|");

        let result = await proxy.Request({
            url: proxy.Api.saveUpdate,
            params,
        });
        if (!result) {
            return;
        }
        formDataRef.value.resetFields();
        dialogConfig.value.show = false;
        emit("reload");
    });


}


const showEdit = (data = {}) => {
    dialogConfig.value.show = true;
    nextTick(() => {
        formDataRef.value.resetFields();
        if (data) {
            data.updateDescList = data.updateDesc.split("|").map(item => ({ title: item }));
            data.fielName = 'PurpleChat.' + data.version + '.exe';
        }
        formData.value = Object.assign({}, data || { updateDescList: [{ title: "" }] });
    })

}


defineExpose({
    showEdit
})

</script>

<style lang="scss" scoped>
.file-select {
    display: flex;

    .file-name {
        color: #409eff;
        margin-right: 10px;
    }
}

.update-form-item {
    margin-bottom: 0px;

    .update-desc-item {
        width: 100%;
        margin-bottom: 15px;

        .update-desc {
            width: 100%;
            display: flex;

            .num {
                width: 15px;
                margin-right: 2px
            }

            .input {
                flex: 1
            }

            .iconfont {
                cursor: pointer;
                margin-left: 10px;
                text-align: center;
            }

            .reduce {
                font-size: small;
                margin-left: 0;
                padding-left: 1px;
            }
        }
    }
}
</style>
