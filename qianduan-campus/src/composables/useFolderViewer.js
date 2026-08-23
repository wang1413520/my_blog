import { computed, ref } from 'vue'

export function useFolderViewer() {
  const folderViewerVisible = ref(false)
  const activeFolderGroupKey = ref('')
  const activeFolderGroup = ref(null)
  const folderViewerTreeData = ref([])
  const folderViewerPath = ref([])

  const folderViewerRootNode = computed(() => {
    return folderViewerTreeData.value[0] || null
  })

  const folderViewerPathSegments = computed(() => {
    const segments = []
    let currentChildren = folderViewerRootNode.value?.children || []

    folderViewerPath.value.forEach(nodeId => {
      const nextNode = currentChildren.find(child => child.id === nodeId && child.nodeType === 'folder')
      if (!nextNode) {
        return
      }
      segments.push({
        id: nextNode.id,
        label: nextNode.label
      })
      currentChildren = nextNode.children || []
    })

    return segments
  })

  const currentFolderNode = computed(() => {
    let currentNode = folderViewerRootNode.value
    if (!currentNode) {
      return null
    }

    for (const nodeId of folderViewerPath.value) {
      const nextNode = (currentNode.children || []).find(child => child.id === nodeId && child.nodeType === 'folder')
      if (!nextNode) {
        break
      }
      currentNode = nextNode
    }

    return currentNode
  })

  const currentFolderEntries = computed(() => {
    const children = currentFolderNode.value?.children || []
    return [...children].sort((left, right) => {
      if (left.nodeType !== right.nodeType) {
        return left.nodeType === 'folder' ? -1 : 1
      }
      return String(left.label || '').localeCompare(String(right.label || ''), 'zh-CN')
    })
  })

  function countFolderNodeFiles(node) {
    if (!node?.children?.length) {
      return 0
    }

    return node.children.reduce((sum, child) => {
      if (child.nodeType === 'file') {
        return sum + 1
      }
      return sum + countFolderNodeFiles(child)
    }, 0)
  }

  function getFolderNodeDescription(node) {
    if (!node) {
      return '暂无内容'
    }
    const directChildrenCount = node.children?.length || 0
    const fileCount = countFolderNodeFiles(node)
    if (directChildrenCount === 0) {
      return '空文件夹'
    }
    return `包含 ${directChildrenCount} 项，累计 ${fileCount} 个文件`
  }

  function buildFolderTree(group) {
    const rootNode = {
      id: `root-${group.key}`,
      label: group.folderName,
      nodeType: 'folder',
      children: []
    }

    group.items.forEach(item => {
      const normalizedPath = String(item.relativePath || item.title || '').trim()
      const segments = normalizedPath
        ? normalizedPath.split('/').filter(Boolean)
        : [item.title || `文件-${item.id}`]

      let currentNode = rootNode
      let currentPath = ''

      segments.forEach((segment, index) => {
        currentPath = currentPath ? `${currentPath}/${segment}` : segment
        const isFileNode = index === segments.length - 1

        if (isFileNode) {
          currentNode.children.push({
            id: `file-${item.id}`,
            label: segment,
            nodeType: 'file',
            fileType: item.fileType,
            fileSize: item.fileSize,
            resourceId: item.id,
            raw: item
          })
          return
        }

        let nextNode = currentNode.children.find(child => child.id === `dir-${currentPath}`)
        if (!nextNode) {
          nextNode = {
            id: `dir-${currentPath}`,
            label: segment,
            nodeType: 'folder',
            children: []
          }
          currentNode.children.push(nextNode)
        }
        currentNode = nextNode
      })
    })

    return [rootNode]
  }

  function openFolderViewer(group) {
    activeFolderGroupKey.value = group.key
    activeFolderGroup.value = group
    folderViewerTreeData.value = buildFolderTree(group)
    folderViewerPath.value = []
    folderViewerVisible.value = true
  }

  function refreshActiveFolderViewer(getGroupByKey) {
    if (!folderViewerVisible.value || !activeFolderGroupKey.value) {
      return
    }

    const group = getGroupByKey(activeFolderGroupKey.value)
    if (!group || group.displayType !== 'folder') {
      folderViewerVisible.value = false
      activeFolderGroup.value = null
      activeFolderGroupKey.value = ''
      folderViewerTreeData.value = []
      return
    }

    activeFolderGroup.value = group
    folderViewerTreeData.value = buildFolderTree(group)

    const resolvedPath = []
    let currentNode = folderViewerTreeData.value[0]
    for (const nodeId of folderViewerPath.value) {
      const nextNode = (currentNode?.children || []).find(child => child.id === nodeId && child.nodeType === 'folder')
      if (!nextNode) {
        break
      }
      resolvedPath.push(nextNode.id)
      currentNode = nextNode
    }
    folderViewerPath.value = resolvedPath
  }

  function enterFolderNode(node) {
    if (node.nodeType !== 'folder') {
      return
    }
    folderViewerPath.value = [...folderViewerPath.value, node.id]
  }

  function goToFolderRoot() {
    folderViewerPath.value = []
  }

  function goToFolderLevel(index) {
    folderViewerPath.value = folderViewerPath.value.slice(0, index + 1)
  }

  function goToParentFolder() {
    if (!folderViewerPath.value.length) {
      return
    }
    folderViewerPath.value = folderViewerPath.value.slice(0, -1)
  }

  return {
    folderViewerVisible,
    activeFolderGroupKey,
    activeFolderGroup,
    folderViewerTreeData,
    folderViewerPath,
    folderViewerRootNode,
    folderViewerPathSegments,
    currentFolderNode,
    currentFolderEntries,
    getFolderNodeDescription,
    buildFolderTree,
    openFolderViewer,
    refreshActiveFolderViewer,
    enterFolderNode,
    goToFolderRoot,
    goToFolderLevel,
    goToParentFolder
  }
}
