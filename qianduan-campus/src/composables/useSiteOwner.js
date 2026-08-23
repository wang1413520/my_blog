import { SITE_OWNER } from '@/config/site'

/**
 * 判断某个用户名是否属于站长
 * @param {string} name - 要检查的用户名/昵称
 * @returns {boolean}
 */
export function isSiteOwner(name) {
  if (!name) return false
  return SITE_OWNER.names.some(
    ownerName => ownerName.toLowerCase() === String(name).toLowerCase()
  )
}

export { SITE_OWNER }
