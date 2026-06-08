export const CAT_ICON = {
  // 지출
  '식비':       { icon: '🍚', color: '#FF6B6B' },
  '외식·여가':  { icon: '🍽️', color: '#FF8E53' },
  '교통·유류':  { icon: '🚗', color: '#4ECDC4' },
  '의료비':     { icon: '💊', color: '#45B7D1' },
  '교육비':     { icon: '📚', color: '#96CEB4' },
  '공과금·통신':{ icon: '📱', color: '#6C5CE7' },
  '의류·잡화':  { icon: '👗', color: '#FD79A8' },
  '경조사':     { icon: '🎁', color: '#FDCB6E' },
  '기타지출':   { icon: '💸', color: '#B2BEC3' },
  // 수입
  '농업수입':   { icon: '🌾', color: '#00B894' },
  '급여·연금':  { icon: '💰', color: '#00CEC9' },
  '임대·이자':  { icon: '🏦', color: '#0984E3' },
  '용돈':       { icon: '💵', color: '#6C5CE7' },
  '기타수입':   { icon: '📈', color: '#55EFC4' },
}

export function getCatInfo(category) {
  return CAT_ICON[category] ?? { icon: '💳', color: '#B2BEC3' }
}

export const DAYS = ['일', '월', '화', '수', '목', '금', '토']

export function formatDate(dateStr) {
  const d = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)

  const isToday     = d.toDateString() === today.toDateString()
  const isYesterday = d.toDateString() === yesterday.toDateString()

  const month = d.getMonth() + 1
  const day   = d.getDate()
  const dow   = DAYS[d.getDay()]

  if (isToday)     return `${month}월 ${day}일 오늘`
  if (isYesterday) return `${month}월 ${day}일 어제`
  return `${month}월 ${day}일 ${dow}요일`
}
