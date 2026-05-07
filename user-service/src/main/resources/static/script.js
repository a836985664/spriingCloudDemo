/**
 * 语音转文字核心逻辑 (STT Frontend) - MVP Demo 版本
 * 负责处理麦克风访问、录音、停止和模拟识别结果。
 * 
 * MVP 说明: 此版本跳过后端 API 调用，直接在前端模拟识别结果，
 * 用于快速验证"说话内容 -> 网页显示"的完整用户体验。
 */

const recordButton = document.getElementById('recordButton');
const statusDiv = document.getElementById('status');
const transcriptDiv = document.getElementById('transcript');
const submitButton = document.getElementById('submitButton');

let mediaRecorder;
let audioChunks = [];
let stream = null;

// 模拟的识别文本库（用于演示）
const mockTexts = [
    "您好，这是语音识别的模拟结果。",
    "您刚才说的是：测试语音转文字功能。",
    "识别成功！这是一个演示文本。",
    "您说的内容已经被成功转换为文字。",
    "这是模拟的语音识别结果，实际使用时需要连接真实的 STT 服务。"
];

// --- 1. 权限请求与初始化 ---
async function startRecording() {
    // 检查浏览器是否支持 MediaRecorder
    if (!('MediaRecorder' in window)) {
        alert("抱歉，您的浏览器不支持 MediaRecorder API，无法进行录音。");
        return;
    }

    try {
        // 1. 请求麦克风权限
        stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        
        // 2. 初始化 MediaRecorder
        mediaRecorder = new MediaRecorder(stream);
        audioChunks = [];

        // 3. 事件监听器设置
        mediaRecorder.ondataavailable = event => {
            audioChunks.push(event.data);
        };

        mediaRecorder.onstop = () => {
            // 停止录制后，合并音频数据
            const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
            console.log("录音结束，Blob 大小:", audioBlob.size);
            
            // 更新UI状态
            statusDiv.textContent = "状态: 录音已停止，正在模拟识别...";
            recordButton.textContent = "重新录音";
            recordButton.classList.remove('recording');
            recordButton.classList.add('ready');
            
            // 暴露 Blob 对象供后续处理（这里仅用于演示，实际需传参）
            window.recordedBlob = audioBlob; 
            
            // 🎯 MVP: 自动触发模拟识别（无需点击提交按钮）
            simulateRecognition();
        };

        // 4. 录制开始
        mediaRecorder.start();
        
        // 更新UI
        recordButton.classList.remove('ready');
        recordButton.classList.add('recording');
        recordButton.textContent = "🔴 正在录音... 请说话";
        statusDiv.textContent = "状态: 🟢 正在成功接收麦克风音频流...";
        submitButton.style.display = 'none';
        transcriptDiv.innerHTML = "--- 正在实时（模拟）显示您说的话... ---";

    } catch (err) {
        console.error('录音失败:', err);
        statusDiv.textContent = `状态: 🔴 错误。请检查您的麦克风是否已连接，并确保已授权使用麦克风。 (${err.name})`;
        recordButton.disabled = true;
    }
}

// --- 2. 停止录音 ---
function stopRecording() {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
    }
    // 停止所有媒体流，释放资源
    if (stream) {
        stream.getTracks().forEach(track => track.stop());
    }
}

// --- 3. 🎯 MVP: 模拟识别功能 ---
function simulateRecognition() {
    // 禁用按钮，防止重复操作
    recordButton.disabled = true;
    
    // 模拟 API 调用延迟（1.5秒）
    setTimeout(() => {
        // 随机选择一个模拟文本
        const randomText = mockTexts[Math.floor(Math.random() * mockTexts.length)];
        
        // 更新界面显示
        transcriptDiv.textContent = randomText;
        statusDiv.textContent = "状态: ✅ 模拟识别完成！文本已显示。";
        
        // 恢复按钮状态
        recordButton.disabled = false;
        
        console.log("模拟识别完成，显示文本:", randomText);
    }, 1500);
}

// --- 4. 事件监听器绑定 ---
recordButton.addEventListener('click', () => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        stopRecording();
    } else {
        // 第一次点击或重置点击
        startRecording();
    }
});

// 首次加载时尝试禁用提交按钮
document.addEventListener('DOMContentLoaded', () => {
    submitButton.style.display = 'none';
});