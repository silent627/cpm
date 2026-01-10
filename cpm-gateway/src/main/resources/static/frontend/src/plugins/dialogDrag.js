/**
 * 对话框拖拽和缩放插件
 * 全局自动为所有 Element Plus 对话框添加拖拽和缩放功能
 */

// 初始化对话框拖拽和缩放功能
function initDialog(dialogEl) {
  const dialogHeaderEl = dialogEl.querySelector('.el-dialog__header')
  if (!dialogHeaderEl) {
    return false
  }

  // 标记已初始化，避免重复初始化
  if (dialogEl.dataset.dragInitialized === 'true') {
    return true
  }
  dialogEl.dataset.dragInitialized = 'true'

  // 设置对话框样式（如果还没有设置）
  if (!dialogEl.style.position || dialogEl.style.position === 'static') {
    dialogEl.style.margin = '0'
    dialogEl.style.position = 'absolute'
    dialogEl.style.transition = 'none'
    
    // 计算并设置居中位置
    const computedStyle = window.getComputedStyle(dialogEl)
    const dialogWidth = parseInt(computedStyle.width, 10) || dialogEl.offsetWidth || 500
    const dialogHeight = parseInt(computedStyle.height, 10) || dialogEl.offsetHeight || 400
    
    const centerX = (window.innerWidth - dialogWidth) / 2
    const centerY = (window.innerHeight - dialogHeight) / 2
    
    dialogEl.style.left = centerX + 'px'
    dialogEl.style.top = centerY + 'px'
    dialogEl.style.transform = 'none'
    
    // 如果对话框尺寸还未确定，等待一帧后重新计算
    if (dialogWidth === 500 && dialogHeight === 400) {
      requestAnimationFrame(() => {
        const rect = dialogEl.getBoundingClientRect()
        const actualWidth = rect.width || dialogEl.offsetWidth
        const actualHeight = rect.height || dialogEl.offsetHeight
        
        if (actualWidth !== dialogWidth || actualHeight !== dialogHeight) {
          const newCenterX = (window.innerWidth - actualWidth) / 2
          const newCenterY = (window.innerHeight - actualHeight) / 2
          dialogEl.style.left = newCenterX + 'px'
          dialogEl.style.top = newCenterY + 'px'
        }
      })
    }
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

  return true
}

// 检查并初始化所有对话框
function checkAndInitDialogs() {
  const dialogs = document.querySelectorAll('.el-dialog')
  dialogs.forEach(dialog => {
    if (dialog.dataset.dragInitialized !== 'true') {
      initDialog(dialog)
    }
  })
}

// 全局 observer
let globalObserver = null

export default {
  install(app) {
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
              // 也检查 el-overlay 下的对话框
              if (!dialogEl) {
                const overlay = node.querySelector('.el-overlay')
                if (overlay) {
                  dialogEl = overlay.querySelector('.el-dialog')
                }
              }
            }
            
            if (dialogEl && dialogEl.dataset.dragInitialized !== 'true') {
              // 立即禁用过渡动画，避免移动动画
              dialogEl.style.transition = 'none'
              
              // 立即设置位置（在对话框完全渲染前）
              const computedStyle = window.getComputedStyle(dialogEl)
              const dialogWidth = parseInt(computedStyle.width, 10) || 500
              const dialogHeight = parseInt(computedStyle.height, 10) || 400
              const centerX = (window.innerWidth - dialogWidth) / 2
              const centerY = (window.innerHeight - dialogHeight) / 2
              dialogEl.style.margin = '0'
              dialogEl.style.position = 'absolute'
              dialogEl.style.left = centerX + 'px'
              dialogEl.style.top = centerY + 'px'
              dialogEl.style.transform = 'none'
              
              // 延迟初始化，确保对话框完全渲染后再添加拖拽功能
              setTimeout(() => {
                initDialog(dialogEl)
              }, 50)
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

    // 立即检查一次已存在的对话框
    setTimeout(() => {
      checkAndInitDialogs()
    }, 500)

    // 定期检查（作为备用方案）
    setInterval(() => {
      checkAndInitDialogs()
    }, 1000)
  }
}

