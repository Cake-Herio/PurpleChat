<template>
    <div>
        <el-dialog
            :show-close="showClose"
            :draggable="false"
            :model-value="show"
            :close-on-click-modal="false"
            :title="title"
            class="cust-dialog"
            :top="top+'px'"
            :width="width"
            @close="close"
        >
            <div class="dialog-body" :style="{'max-height': maxHeight + 'px', }">
                <slot></slot>
            </div>
            <template v-if="(buttons && buttons.length>0) || showCancel">
                <div class="dialog-footer">
                    <el-button link @click="close" v-if="showCancel">取消</el-button>
                    <el-button v-for="btn in buttons" :type="btn.type || 'primary'" @click="btn.click" :key="btn.id">
                        {{ btn.text }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    title: {
        type: String,
    },
    width: {
        type: String,
        default: "30%"
    },
    top: {
        type: Number,
        default: 80
    },
    showClose: {
        type: Boolean,
        default: true
    },
    buttons: {
        type: Array,
    },
    showCancel: {
        type: Boolean,
        default: true
    },
    padding: {
        type: Number,
        default: 15
    }
});

const maxHeight = window.innerHeight - props.top - 100;

const emit = defineEmits(['close']);
const close = () => {
    emit('close');
};
</script>

<style lang="scss" scoped>
.cust-dialog {
    .el-dialog {
        padding: 0px;
    }

    .dialog-body {
        border-top: 1px solid #ddd;
        padding-top: 15px;
        border-bottom: 1px solid #ddd;
        min-height: 80px;
        overflow: auto;
        overflow-x: hidden;
    }

    .dialog-footer {
        text-align: right;
        padding: 10px 20px;
    }
}
</style>
