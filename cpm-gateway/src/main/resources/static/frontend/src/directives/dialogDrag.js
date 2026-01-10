/**
 * 对话框拖拽和缩放指令
 * 使用方法：在 el-dialog 上添加 v-dialog-drag 指令
 * 
 * 注意：Element Plus 的对话框使用 Teleport 渲染到 body 下，
 * 所以需要通过监听 DOM 变化来找到对话框元素
 */

// 初始化对话框拖拽和缩放功能
function initDialog(dialogEl) {
  const dialogHeaderEl = dialogEl.querySelector('.el-dialog__header')
  if (!dialogHeaderEl) {
    return
  }

  // 标记已初始化，避免重复初始化
  if (dialogEl.dataset.dragInitialized === 'true') {
    return
  }
  dialogEl.dataset.dragInitialized = 'true'

  // 设置对话框样式
  dialogEl.style.margin = '0'
  if (!dialogEl.style.position || dialogEl.style.position === 'static') {
    dialogEl.style.position = 'absolute'
  }

  // 拖拽功能
  dialogHeaderEl.style.cursor = 'move'
  dialogHeaderEl.style.userSelect = 'none'
  
  let isDragging = false
  let currentX = 0
  let currentY = 0
  let initialX = 0
  let initialY = 0

  const dragStart = (e) => {
    // 如果点击的是关闭按钮或其他交互元素，不触发拖拽
    if (e.target.closest('.el-dialog__close') || 
        e.target.closest('button') || 
        e.target.closest('.el-dialog__headerbtn')) {
      return
    }
    if (e.button !== 0) return // 只响应左键
    isDragging = true
    const rect = dialogEl.getBoundingClientRect()
    currentX = rect.left
    currentY = rect.top
    initialX = e.clientX - currentX
    initialY = e.clientY - currentY
    dialogHeaderEl.style.cursor = 'move'
    e.preventDefault()
    e.stopPropagation()
  }

  const drag = (e) => {
    if (!isDragging) return
    e.preventDefault()
    currentX = e.clientX - initialX
    currentY = e.clientY - initialY
    
    const maxX = window.innerWidth - dialogEl.offsetWidth
    const maxY = window.innerHeight - dialogEl.offsetHeight
    
    currentX = Math.max(0, Math.min(currentX, maxX))
    currentY = Math.max(0, Math.min(currentY, maxY))
    
    dialogEl.style.left = currentX + 'px'
    dialogEl.style.top = currentY + 'px'
    dialogEl.style.transform = 'none'
    dialogEl.style.margin = '0'
  }

  const dragEnd = () => {
    isDragging = false
    dialogHeaderEl.style.cursor = 'move'
  }

  dialogHeaderEl.addEventListener('mousedown', dragStart)
  document.addEventListener('mousemove', drag)
  document.addEventListener('mouseup', dragEnd)

  // 缩放功能
  let resizeHandle = dialogEl.querySelector('.dialog-resize-handle')
  if (!resizeHandle) {
    resizeHandle = document.createElement('div')
    resizeHandle.className = 'dialog-resize-handle'
    resizeHandle.style.cssText = `
      position: absolute;
      right: 0;
      bottom: 0;
      width: 20px;
      height: 20px;
      cursor: nwse-resize;
      background: linear-gradient(-45deg, transparent 30%, #dcdfe6 30%, #dcdfe6 35%, transparent 35%, transparent 65%, #dcdfe6 65%, #dcdfe6 70%, transparent 70%);
      z-index: 1000;
      user-select: none;
    `
    if (!dialogEl.style.position || dialogEl.style.position === 'static') {
      dialogEl.style.position = 'relative'
    }
    dialogEl.appendChild(resizeHandle)
  }

  let isResizing = false
  let startX = 0
  let startY = 0
  let startWidth = 0
  let startHeight = 0

  const resizeStart = (e) => {
    if (e.button !== 0) return
    isResizing = true
    startX = e.clientX
    startY = e.clientY
    const computedStyle = window.getComputedStyle(dialogEl)
    startWidth = parseInt(computedStyle.width, 10) || dialogEl.offsetWidth
    startHeight = parseInt(computedStyle.height, 10) || dialogEl.offsetHeight
    e.preventDefault()
    e.stopPropagation()
  }

  const resize = (e) => {
    if (!isResizing) return
    const width = startWidth + e.clientX - startX
    const height = startHeight + e.clientY - startY
    
    const minWidth = 400
    const minHeight = 300
    const maxWidth = window.innerWidth - 20
    const maxHeight = window.innerHeight - 20
    
    const newWidth = Math.max(minWidth, Math.min(width, maxWidth))
    const newHeight = Math.max(minHeight, Math.min(height, maxHeight))
    
    dialogEl.style.width = newWidth + 'px'
    dialogEl.style.height = newHeight + 'px'
    dialogEl.style.maxWidth = 'none'
    dialogEl.style.maxHeight = 'none'
  }

  const resizeEnd = () => {
    isResizing = false
  }

  resizeHandle.addEventListener('mousedown', resizeStart)
  document.addEventListener('mousemove', resize)
  document.addEventListener('mouseup', resizeEnd)

  // 保存清理函数到对话框元素上
  dialogEl._dialogDragCleanup = () => {
    dialogHeaderEl.removeEventListener('mousedown', dragStart)
    document.removeEventListener('mousemove', drag)
    document.removeEventListener('mouseup', dragEnd)
    if (resizeHandle) {
      resizeHandle.removeEventListener('mousedown', resizeStart)
    }
    document.removeEventListener('mousemove', resize)
    document.removeEventListener('mouseup', resizeEnd)
    if (resizeHandle && resizeHandle.parentNode) {
      resizeHandle.parentNode.removeChild(resizeHandle)
    }
    delete dialogEl.dataset.dragInitialized
  }
}

// 全局 observer 实例（单例模式）
let globalObserver = null
let observerCount = 0
let checkInterval = null

function createObserver() {
  if (globalObserver) {
    return
  }

  // 使用 MutationObserver 监听 body 下的对话框创建
  globalObserver = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      mutation.addedNodes.forEach((node) => {
        if (node.nodeType === 1) {
          // 检查是否是对话框容器
          let dialogEl = null
          if (node.classList && node.classList.contains('el-dialog')) {
            dialogEl = node
          } else if (node.querySelector) {
            // 检查节点内部是否有对话框
            dialogEl = node.querySelector('.el-dialog')
            // 也检查节点本身是否包含对话框类
            if (!dialogEl && node.classList) {
              const overlay = node.querySelector('.el-overlay')
              if (overlay) {
                dialogEl = overlay.querySelector('.el-dialog')
              }
            }
          }
          
          if (dialogEl && dialogEl.dataset.dragInitialized !== 'true') {
            // 延迟初始化，确保对话框完全渲染
            setTimeout(() => {
              initDialog(dialogEl)
            }, 100)
          }
        }
      })
    })
  })

  // 开始观察 body 的变化
  globalObserver.observe(document.body, {
    childList: true,
    subtree: true
  })

  // 定期检查已存在的对话框（作为备用方案）
  checkInterval = setInterval(() => {
    const dialogs = document.querySelectorAll('.el-dialog')
    dialogs.forEach(dialog => {
      if (dialog.dataset.dragInitialized !== 'true') {
        initDialog(dialog)
      }
    })
  }, 500)
}

function destroyObserver() {
  if (observerCount <= 0 && globalObserver) {
    globalObserver.disconnect()
    globalObserver = null
  }
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
}

export default {
  mounted() {
    observerCount++
    createObserver()
    
    // 立即检查一次
    setTimeout(() => {
      const dialogs = document.querySelectorAll('.el-dialog')
      dialogs.forEach(dialog => {
        if (dialog.dataset.dragInitialized !== 'true') {
          initDialog(dialog)
        }
      })
    }, 500)
  },

  unmounted() {
    observerCount--
    destroyObserver()
  }
}

