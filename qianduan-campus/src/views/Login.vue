<template>
  <div class="login-container">
    <div class="animated-bg" aria-hidden="true">
      <canvas ref="particlesCanvasRef" class="particles-layer"></canvas>
      <span
        v-for="star in largeShootingStars"
        :key="star.id"
        class="shooting-star shooting-star-large"
        :style="{
          '--star-top': star.top,
          '--star-delay': star.delay,
          '--star-duration': star.duration,
          '--star-length': star.length,
          '--star-drift': star.drift,
          '--star-drop': star.drop,
          '--tail-bend': star.tailBend
        }"
      ></span>
      <span
        v-for="star in smallShootingStars"
        :key="star.id"
        class="shooting-star shooting-star-small"
        :style="{
          '--star-top': star.top,
          '--star-delay': star.delay,
          '--star-duration': star.duration,
          '--star-length': star.length,
          '--star-drift': star.drift,
          '--star-drop': star.drop,
          '--tail-bend': star.tailBend
        }"
      ></span>
      <div class="bg-vignette"></div>
    </div>

    <div class="login-box">
      <div class="logo-section">
        <h1 class="title">小怀风的博客</h1>
        <p class="subtitle">记录技术、学习和生活里的想法</p>
      </div>

      <div class="login-tabs" role="tablist" aria-label="登录注册切换">
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'login' }"
          @click="activeTab = 'login'"
        >
          登录
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'register' }"
          @click="activeTab = 'register'"
        >
          注册
        </button>
      </div>

      <div v-if="activeTab === 'login'" class="tab-panel">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="form-content"
          @submit.prevent
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loginLoading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>
      </div>

      <div v-else class="tab-panel">
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="form-content"
          @submit.prevent
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（4-20 位）"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6-20 位）"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称（可选）"
              :prefix-icon="Avatar"
              size="large"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="registerLoading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Avatar, Lock, User } from '@element-plus/icons-vue'
import { userAPI } from '@/api/user'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginFormRef = ref(null)
const registerFormRef = ref(null)
const particlesCanvasRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const largeShootingStars = [
  { id: 'large-star-1', top: '3%', delay: '-0.4s', duration: '4.8s', length: '240px', drift: '136vw', drop: '30vh', tailBend: '9deg' },
  { id: 'large-star-2', top: '10%', delay: '-2.2s', duration: '5.4s', length: '280px', drift: '148vw', drop: '34vh', tailBend: '13deg' },
  { id: 'large-star-3', top: '18%', delay: '-1.1s', duration: '5.1s', length: '220px', drift: '132vw', drop: '28vh', tailBend: '8deg' },
  { id: 'large-star-4', top: '27%', delay: '-3.8s', duration: '5.8s', length: '260px', drift: '154vw', drop: '36vh', tailBend: '15deg' },
  { id: 'large-star-5', top: '38%', delay: '-0.9s', duration: '5.2s', length: '250px', drift: '144vw', drop: '31vh', tailBend: '10deg' },
  { id: 'large-star-6', top: '50%', delay: '-4.6s', duration: '6s', length: '290px', drift: '158vw', drop: '37vh', tailBend: '16deg' },
  { id: 'large-star-7', top: '63%', delay: '-2.9s', duration: '5.6s', length: '230px', drift: '140vw', drop: '29vh', tailBend: '9deg' },
  { id: 'large-star-8', top: '74%', delay: '-5.1s', duration: '6.2s', length: '265px', drift: '150vw', drop: '35vh', tailBend: '14deg' }
]

const smallShootingStars = [
  { id: 'small-star-1', top: '6%', delay: '-0.8s', duration: '3.2s', length: '112px', drift: '118vw', drop: '20vh', tailBend: '7deg' },
  { id: 'small-star-2', top: '12%', delay: '-1.6s', duration: '3.5s', length: '96px', drift: '122vw', drop: '19vh', tailBend: '5deg' },
  { id: 'small-star-3', top: '20%', delay: '-2.4s', duration: '3.1s', length: '104px', drift: '116vw', drop: '18vh', tailBend: '6deg' },
  { id: 'small-star-4', top: '25%', delay: '-0.3s', duration: '3.6s', length: '118px', drift: '126vw', drop: '21vh', tailBend: '8deg' },
  { id: 'small-star-5', top: '31%', delay: '-2.8s', duration: '3.4s', length: '90px', drift: '114vw', drop: '17vh', tailBend: '4deg' },
  { id: 'small-star-6', top: '36%', delay: '-1.2s', duration: '3s', length: '100px', drift: '120vw', drop: '19vh', tailBend: '6deg' },
  { id: 'small-star-7', top: '43%', delay: '-3.2s', duration: '3.7s', length: '110px', drift: '128vw', drop: '22vh', tailBend: '8deg' },
  { id: 'small-star-8', top: '49%', delay: '-0.6s', duration: '3.3s', length: '94px', drift: '117vw', drop: '18vh', tailBend: '5deg' },
  { id: 'small-star-9', top: '57%', delay: '-2s', duration: '3.5s', length: '106px', drift: '124vw', drop: '20vh', tailBend: '7deg' },
  { id: 'small-star-10', top: '64%', delay: '-3.6s', duration: '3.1s', length: '98px', drift: '119vw', drop: '18vh', tailBend: '5deg' },
  { id: 'small-star-11', top: '71%', delay: '-1.5s', duration: '3.4s', length: '108px', drift: '123vw', drop: '21vh', tailBend: '7deg' },
  { id: 'small-star-12', top: '79%', delay: '-2.7s', duration: '3.2s', length: '92px', drift: '115vw', drop: '17vh', tailBend: '4deg' },
  { id: 'small-star-13', top: '15%', delay: '-4.1s', duration: '3.6s', length: '102px', drift: '127vw', drop: '22vh', tailBend: '8deg' },
  { id: 'small-star-14', top: '46%', delay: '-4.8s', duration: '3.3s', length: '88px', drift: '113vw', drop: '16vh', tailBend: '4deg' },
  { id: 'small-star-15', top: '68%', delay: '-5.4s', duration: '3.7s', length: '114px', drift: '129vw', drop: '23vh', tailBend: '9deg' }
]

const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
    return
  }

  if (value.length < 4 || value.length > 20) {
    callback(new Error('用户名长度需为 4 到 20 位'))
    return
  }

  if (!/^[a-zA-Z0-9]+$/.test(value)) {
    callback(new Error('用户名只能包含字母和数字'))
    return
  }

  callback()
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }

  if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度需为 6 到 20 位'))
    return
  }

  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }

  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

const loginRules = {
  username: [{ required: true, validator: validateUsername, trigger: 'blur' }],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, validator: validateUsername, trigger: 'blur' }],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

let particleContext = null
let particleFrameId = 0
let particleField = []

const createParticle = (width, height) => ({
  x: Math.random() * width,
  y: Math.random() * height,
  radius: Math.random() * 1.8 + 0.5,
  speedX: Math.random() * 0.28 + 0.08,
  speedY: Math.random() * 0.16 + 0.04,
  alpha: Math.random() * 0.45 + 0.12
})

const resizeParticleField = () => {
  const canvas = particlesCanvasRef.value
  if (!canvas) {
    return
  }

  const ratio = window.devicePixelRatio || 1
  const { width, height } = canvas.getBoundingClientRect()

  canvas.width = Math.max(1, Math.floor(width * ratio))
  canvas.height = Math.max(1, Math.floor(height * ratio))

  particleContext = canvas.getContext('2d')
  if (!particleContext) {
    return
  }

  particleContext.setTransform(1, 0, 0, 1, 0, 0)
  particleContext.scale(ratio, ratio)

  const particleCount = Math.max(36, Math.floor((width * height) / 26000))
  particleField = Array.from({ length: particleCount }, () => createParticle(width, height))
}

const drawParticleFrame = () => {
  const canvas = particlesCanvasRef.value
  if (!canvas || !particleContext) {
    return
  }

  const { width, height } = canvas.getBoundingClientRect()
  particleContext.clearRect(0, 0, width, height)

  for (const particle of particleField) {
    particle.x += particle.speedX
    particle.y += particle.speedY

    if (particle.x - particle.radius > width || particle.y - particle.radius > height) {
      Object.assign(particle, createParticle(width, height), {
        x: Math.random() * width * 0.25,
        y: -particle.radius
      })
    }

    particleContext.beginPath()
    particleContext.fillStyle = `rgba(236, 242, 248, ${particle.alpha})`
    particleContext.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2)
    particleContext.fill()
  }

  particleFrameId = window.requestAnimationFrame(drawParticleFrame)
}

const initParticles = () => {
  resizeParticleField()

  if (!particleContext) {
    return
  }

  if (particleFrameId) {
    window.cancelAnimationFrame(particleFrameId)
  }

  drawParticleFrame()
  window.addEventListener('resize', resizeParticleField)
}

const destroyParticles = () => {
  if (particleFrameId) {
    window.cancelAnimationFrame(particleFrameId)
    particleFrameId = 0
  }

  window.removeEventListener('resize', resizeParticleField)
  particleField = []
  particleContext = null
}

onMounted(() => {
  initParticles()
})

onBeforeUnmount(() => {
  destroyParticles()
})

const handleLogin = async () => {
  if (!loginFormRef.value) {
    return
  }

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  loginLoading.value = true
  try {
    const data = await userAPI.login(loginForm)
    userStore.setToken(data.token)
    userStore.setUserInfo({
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      role: data.role
    })
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error?.response?.data?.message || error?.message || '登录失败，请稍后重试')
  } finally {
    loginLoading.value = false
  }
}

const resetRegisterForm = () => {
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.nickname = ''
  registerFormRef.value?.clearValidate()
}

const handleRegister = async () => {
  if (!registerFormRef.value) {
    return
  }

  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  registerLoading.value = true
  try {
    await userAPI.register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || undefined
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    resetRegisterForm()
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error?.response?.data?.message || error?.message || '注册失败，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.08), transparent 42%),
    radial-gradient(circle at 20% 20%, rgba(120, 130, 145, 0.14), transparent 28%),
    linear-gradient(180deg, #07090d 0%, #11151c 48%, #050608 100%);
}

.animated-bg {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.particles-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.bg-vignette {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at center, transparent 0, transparent 52%, rgba(3, 4, 7, 0.5) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), rgba(2, 4, 8, 0.38));
}

.shooting-star {
  position: absolute;
  top: var(--star-top);
  left: -26%;
  width: var(--star-length);
  border-radius: 999px;
  opacity: 0;
  transform: translate3d(0, 0, 0) rotate(-29deg) scaleX(0.68);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(241, 245, 249, 0.95));
  animation: shooting-star var(--star-duration) linear infinite;
  animation-delay: var(--star-delay);
  will-change: transform, opacity;
}

.shooting-star::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 8%;
  width: 84%;
  height: 220%;
  border-radius: 999px 999px 120% 160%;
  transform-origin: right center;
  transform: translateY(-50%) skewY(calc(var(--tail-bend) * -1)) rotate(calc(var(--tail-bend) * 0.45));
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0),
    rgba(226, 232, 240, 0.12) 26%,
    rgba(241, 245, 249, 0.38) 70%,
    rgba(255, 255, 255, 0.08)
  );
  filter: blur(5px);
  opacity: 0.9;
}

.shooting-star-large {
  height: 2.2px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(241, 245, 249, 0.98));
  box-shadow: 0 0 18px rgba(255, 255, 255, 0.42), 0 0 28px rgba(255, 255, 255, 0.24);
}

.shooting-star-small {
  height: 1.2px;
  opacity: 0;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(226, 232, 240, 0.9));
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.25), 0 0 18px rgba(255, 255, 255, 0.12);
}

.shooting-star-large::before {
  height: 260%;
  filter: blur(7px);
  opacity: 0.95;
}

.shooting-star-small::before {
  width: 78%;
  height: 180%;
  filter: blur(3px);
  opacity: 0.72;
}

.shooting-star::after {
  content: '';
  position: absolute;
  top: 50%;
  right: 0;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.7), 0 0 32px rgba(255, 255, 255, 0.3);
}

.shooting-star-small::after {
  width: 4px;
  height: 4px;
  background: rgba(241, 245, 249, 0.9);
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.35), 0 0 20px rgba(255, 255, 255, 0.16);
}

@keyframes shooting-star {
  0% {
    opacity: 0;
    transform: translate3d(-4vw, -2vh, 0) rotate(-30deg) scaleX(0.62);
  }

  8% {
    opacity: 0.96;
    transform: translate3d(14vw, 1.5vh, 0) rotate(-28deg) scaleX(0.9);
  }

  36% {
    opacity: 0.9;
    transform: translate3d(calc(var(--star-drift) * 0.38), calc(var(--star-drop) * 0.28), 0) rotate(-24deg) scaleX(1);
  }

  72% {
    opacity: 0.52;
    transform: translate3d(calc(var(--star-drift) * 0.8), calc(var(--star-drop) * 0.82), 0) rotate(-17deg) scaleX(0.95);
  }

  100% {
    opacity: 0;
    transform: translate3d(var(--star-drift), var(--star-drop), 0) rotate(-12deg) scaleX(0.88);
  }
}

.login-box {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  background: rgba(14, 18, 24, 0.76);
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.42);
  backdrop-filter: blur(18px);
  animation: fade-in-scale 0.6s ease-out;
}

@keyframes fade-in-scale {
  0% {
    opacity: 0;
    transform: scale(0.92) translateY(20px);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.logo-section {
  margin-bottom: 30px;
  text-align: center;
}

.title {
  margin: 0;
  color: #f8fafc;
  font-size: 36px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0.06em;
}

.subtitle {
  margin-top: 10px;
  color: rgba(226, 232, 240, 0.62);
  font-size: 14px;
}

.login-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
}

.tab-btn {
  position: relative;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: rgba(203, 213, 225, 0.6);
  font-size: 16px;
  font-weight: 600;
  background: transparent;
  transition: all 0.3s ease;
}

.tab-btn::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: -11px;
  left: 0;
  height: 3px;
  border-radius: 999px;
  transform: scaleX(0);
  transform-origin: center;
  background: linear-gradient(90deg, #f8fafc 0%, #94a3b8 100%);
  transition: transform 0.3s ease;
}

.tab-btn:hover {
  transform: translateY(-1px);
  color: #f8fafc;
  background: rgba(255, 255, 255, 0.05);
}

.tab-btn.active {
  color: #f8fafc;
  background: rgba(255, 255, 255, 0.1);
}

.tab-btn.active::after {
  transform: scaleX(1);
}

.tab-panel {
  min-height: 0;
  animation: fade-in 0.4s ease-out;
}

@keyframes fade-in {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-content :deep(.el-form-item) {
  margin-bottom: 24px;
}

.form-content :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.05);
  transition: all 0.3s;
}

.form-content :deep(.el-input__wrapper:hover) {
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.04);
}

.form-content :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    inset 0 0 0 1px rgba(226, 232, 240, 0.18),
    0 0 0 4px rgba(148, 163, 184, 0.1);
}

.form-content :deep(.el-input__inner) {
  color: #f8fafc;
}

.form-content :deep(.el-input__inner::placeholder) {
  color: rgba(203, 213, 225, 0.46);
}

.form-content :deep(.el-input__prefix-inner) {
  color: rgba(203, 213, 225, 0.6);
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 10px;
  border: none;
  border-radius: 10px;
  color: #10141b;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #f8fafc 0%, #8f98a3 100%);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(0, 0, 0, 0.32);
}

.submit-btn:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .shooting-star {
    width: calc(var(--star-length) * 0.75);
  }

  .login-box {
    width: calc(100vw - 32px);
    padding: 32px 24px;
  }
}
</style>
