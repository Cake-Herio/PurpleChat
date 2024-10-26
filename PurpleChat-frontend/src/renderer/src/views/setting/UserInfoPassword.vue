<template>

    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
        <div class="form">
            <el-form-item label="原密码" prop="originPassword" v-if="!isForget">
                <el-input type="password" clearable placeholder="请输入原密码" v-model.trim="formData.originPassword"
                    show-password></el-input>
            </el-form-item>


            <el-form-item prop="email" v-if="isForget" label="邮箱">
                <el-input clearable placeholder="请输入邮箱" v-model.trim="formData.email" maxlength="30"
                    >
                </el-input>
            </el-form-item>

            <el-form-item prop="emailCheck" v-if="isForget" label="验证码">
                <div class="check-code-panel">
                    <el-input clearable placeholder="请输入邮箱验证码" v-model.trim="formData.emailCheck" maxlength="30"
                        >
                    </el-input>

                    <el-button type="primary" @click="submitEmailCheckCode" :disabled="disableButton">
                        获取
                    </el-button>
                </div>
            </el-form-item>



            <el-form-item label="新密码" prop="password">
                <el-input type="password" clearable placeholder="请输入新密码" v-model.trim="formData.password"
                    show-password></el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="rePassword">
                <el-input type="password" clearable placeholder="请再次输入新密码" v-model.trim="formData.rePassword"
                    show-password></el-input>
            </el-form-item>
        </div>

        <el-form-item>
            <el-button type="primary" @click="saveUserInfo">修改密码</el-button>
            <el-button link @click="cancel">取消</el-button>

            <span class="a-link" @click="forget" v-if="!isForget">忘记密码?</span>
        </el-form-item>
    </el-form>


</template>

<script setup>
import { ref, getCurrentInstance } from "vue";
import { useRoute, useRouter } from "vue-router";
const { proxy } = getCurrentInstance();
const route = useRoute();
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();
const myEmail = dynamicStore.getData("email")
const router = useRouter();
const isForget = ref(false);
const formData = ref({
    password: '',
    rePassword: ''
});
const disableButton = ref(false)
const forget = () => {
    isForget.value = true
}

const formDataRef = ref(null);

const checkyourRepassword = (rule, value, callback) => {
    if (value !== formData.value.password) {
        callback(new Error('两次输入的密码不一致'));
    } else {
        callback();
    }
};

const submitEmailCheckCode = async () => {
    if (formData.value.email == null) {
        proxy.Message.error('邮箱不能为空')
        return
    } else if (formData.value.email != myEmail) {
        proxy.Message.error('不是本人邮箱,请输入本人邮箱')
        return
    }
    else {
        if (disableButton.value) {
            proxy.Message.warning('请等一下再请求')
            return
        }

        disableButton.value = true
        setTimeout(() => {
            disableButton.value = false
        }, 60000)

        //传到后端
        let result = await proxy.Request({
            url: proxy.Api.getEmailCode,
            showLoading: true,
            showerror: false,
            params: {
                email: formData.value.email,
                isloginByEmail: false
            }
        })
        if (!result) {
            return
        }
        proxy.Message.success('已发送')
        localStorage.setItem('checkEmailCodeKey', result.data.checkEmailCodeKey)
    }

}

const rules = {
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
    ],
    emailCheck: [
        { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
    ],
    originPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        {
            pattern: /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/,
            message: '密码须是数字、大小写字母、特殊字符8~18位',
            trigger: 'blur'
        }
    ],
    rePassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: checkyourRepassword, trigger: 'blur' }
    ],
};

const emit = defineEmits(['editBack']);
const saveUserInfo = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        if (isForget.value) {
            proxy.Confirm({
                message: "密码重置后将重新登陆",
                okfun: async () => {
                    let checkEmailCodeKey = localStorage.getItem('checkEmailCodeKey')
                    let result = await proxy.Request({
                        url: proxy.Api.forgetPassword,
                        params: {
                            email: formData.value.email,
                            checkEmailCode: formData.value.emailCheck,
                            checkEmailCodeKey: checkEmailCodeKey,
                            password: formData.value.password
                        }
                    })
                    if (!result) {
                        return;
                    }
                    proxy.Message.success("重置成功 请重新登录");
                    window.ipcRenderer.send('relogin');
                }
            });

        }else{
             //判断原始密码是否和当前密码一致，若一致则不允许修改
        if (formData.value.originPassword === formData.value.password) {
            proxy.Message.error("新密码不能和原密码一致");
            return;
        }




        proxy.Confirm({
            message: "确认修改密码吗？稍后需重新登录",
            okfun: async () => {
                let params = {};
                Object.assign(params, formData.value);
                let result = await proxy.Request({
                    url: proxy.Api.updatePassword,
                    params,
                });
                if (!result) {
                    return;
                }


                proxy.Message.success("修改成功 请重新登录");

                window.ipcRenderer.send('relogin');
            }
        });

        }

    });
};

const cancel = () => {
    emit('editBack');
};
</script>

<style lang="scss" scoped>
.form {
    padding: 0px, 15px, 29px, 15px;

    :deep(.el-input__wrapper) {
        box-shadow: none;
        border-radius: none;
        background-color: transparent; //
    }

    .el-form-item {
        border-bottom: 1px solid #ddd;
    }


}

.a-link {

    color: #e7c2ff;
    cursor: pointer;
    //贴着最右边
    float: right;
    margin-left: 100px;
    font-size: small;
}

.check-code-panel {
    display: flex;

    .check-code {
        cursor: pointer;
        width: 120px;
        margin-left: 5px;
    }
}
</style>