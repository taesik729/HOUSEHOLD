-- 1. 테이블 생성
create table if not exists household_ledger (
  id         uuid primary key default gen_random_uuid(),
  user_id    uuid references auth.users(id) on delete cascade not null,
  date       date not null,
  type       text not null check (type in ('income','expense')),
  category   text not null,
  amount     integer not null check (amount > 0),
  memo       text,
  created_at timestamptz default now()
);

-- 2. RLS 활성화 (본인 데이터만 접근)
alter table household_ledger enable row level security;

create policy "본인 조회" on household_ledger for select using (auth.uid() = user_id);
create policy "본인 입력" on household_ledger for insert with check (auth.uid() = user_id);
create policy "본인 수정" on household_ledger for update using (auth.uid() = user_id);
create policy "본인 삭제" on household_ledger for delete using (auth.uid() = user_id);

-- 3. 인덱스
create index on household_ledger(user_id, date desc);
