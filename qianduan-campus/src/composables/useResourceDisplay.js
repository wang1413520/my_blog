import pdfCover from '@/assets/resource-covers/pdf.svg'
import docCover from '@/assets/resource-covers/doc.svg'
import docxCover from '@/assets/resource-covers/docx.svg'
import genericCover from '@/assets/resource-covers/generic.svg'

const coverMap = {
  pdf: pdfCover,
  doc: docCover,
  docx: docxCover
}

export function getNormalizedFileType(fileType) {
  return String(fileType || '').trim().toLowerCase()
}

export function getResourceCover(fileType) {
  return coverMap[getNormalizedFileType(fileType)] || genericCover
}

export function getDisplayFileType(fileType) {
  const normalized = getNormalizedFileType(fileType)
  return normalized ? normalized.toUpperCase() : 'FILE'
}

export function getFileExtension(fileName) {
  const normalizedName = String(fileName || '').trim()
  const lastDotIndex = normalizedName.lastIndexOf('.')
  if (lastDotIndex === -1) {
    return ''
  }
  return normalizedName.slice(lastDotIndex + 1).toLowerCase()
}

export function getFileTitle(fileName) {
  const normalizedName = String(fileName || '').trim()
  const lastDotIndex = normalizedName.lastIndexOf('.')
  if (lastDotIndex === -1) {
    return normalizedName
  }
  return normalizedName.slice(0, lastDotIndex)
}

export function getResourceDescription(item) {
  if (item.displayType === 'folder') {
    if (item.description) {
      return item.description
    }
    return `共 ${item.fileCount} 个文件，点击后可逐层展开目录`
  }
  return item.description || '暂无描述'
}

export function buildDisplayResourceList(sourceList) {
  const result = []
  const folderGroupMap = new Map()

  sourceList.forEach(item => {
    if (!(item.uploadType === 'folder' && item.folderName)) {
      result.push({
        ...item,
        displayType: 'file',
        key: `file-${item.id}`
      })
      return
    }

    const groupIdentity = item.batchNo || `${item.folderName}-${item.uploaderName || 'anonymous'}-${item.createTime || ''}`
    const groupKey = `folder-${groupIdentity}`

    if (!folderGroupMap.has(groupKey)) {
      const group = {
        key: groupKey,
        displayType: 'folder',
        folderName: item.folderName,
        batchNo: item.batchNo || '',
        uploaderName: item.uploaderName,
        description: item.description || '',
        createTime: item.createTime,
        items: [],
        totalSize: 0,
        downloadCount: 0
      }
      folderGroupMap.set(groupKey, group)
      result.push(group)
    }

    const group = folderGroupMap.get(groupKey)
    group.items.push(item)
    group.totalSize += Number(item.fileSize || 0)
    group.downloadCount += Number(item.downloadCount || 0)

    if (!group.description && item.description) {
      group.description = item.description
    }
    if ((item.createTime || '') > (group.createTime || '')) {
      group.createTime = item.createTime
    }
  })

  return result.map(item => {
    if (item.displayType !== 'folder') {
      return item
    }

    const previewPaths = item.items
      .map(child => child.relativePath)
      .filter(Boolean)
      .slice(0, 3)

    return {
      ...item,
      fileCount: item.items.length,
      previewPaths,
      remainingCount: Math.max(item.items.length - previewPaths.length, 0)
    }
  })
}

