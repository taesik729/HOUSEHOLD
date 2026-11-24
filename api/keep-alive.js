import { createClient } from '@supabase/supabase-js'

const supabase = createClient(
  process.env.VITE_SUPABASE_URL,
  process.env.VITE_SUPABASE_ANON_KEY
)

export default async function handler(req, res) {
  try {
    await supabase.from('household_ledger').select('id').limit(1)
    res.status(200).json({ ok: true, ts: new Date().toISOString() })
  } catch (e) {
    res.status(500).json({ ok: false, error: e.message })
  }
}
