/**
 * 站点全局配置
 *
 * 吉祥物徽章匹配规则：
 * 文章/评论中的 authorName 可能是昵称也可能是用户名，
 * 所以把两者都放进 names，任一个命中即显示徽章。
 */
export const SITE_OWNER = {
  names: [
    'xiaohuaifeng',        // 用户名（唯一）
    '大怀风（＾ω＾）',    // 昵称
  ],
  badgeText: '吉祥物',
  badgeTheme: 'gold'
}
