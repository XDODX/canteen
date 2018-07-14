# canteen

select r.id,
       r.activity_id,
       r.round,
       r.round_per_day,
       r.starttime + 12 / 24 as starttime,
       r.endtime + 12 / 24 as endtime,
       r.total_ticket_num,
       r.ticket_type,
       c.name as ticketname,
       r.require_bet,
       r.max_ticket_per_player,
       r.total_race_num,
       r.race_room_id,
       r.flag,
       r.roundday,
       r.alloc_ticket_num,
       r.AWARD_TYPE
  from t_as_race_round r
  left join t_race_ticket_config c
    on r.ticket_type = c.ticket_type
 where r.flag = 0
   and r.activity_id = '18052814000001'
 order by round
