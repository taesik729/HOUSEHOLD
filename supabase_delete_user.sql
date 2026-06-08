-- 회원 탈퇴용 RPC (본인 계정만 삭제 가능)
create or replace function delete_user()
returns void
language plpgsql
security definer
as $$
begin
  delete from auth.users where id = auth.uid();
end;
$$;
