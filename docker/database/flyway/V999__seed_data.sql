-- Paswords: password1, password2, password3
insert into "user" values (default, 'user1', '$2a$10$QL45PqKz2/CnLpFaY5IDnOhdsYp.lv4/csKvreNQKY1NVJv1dnYXC', 'user1@email.com', '2018-12-22 00:00:00', null, 'ROLE_USER,ROLE_ADMIN');
insert into "user" values (default, 'user2', '$2a$10$fX0J7FSXfr/9Hs.bJd0bSeZXS918BbCinoFHR5hCntGuS56Km1jwS', 'user2@email.com', '2017-12-22 00:00:00', null, 'ROLE_USER');
insert into "user" values (default, 'user3', '$2a$10$OG.sjGJXzFhpJA3QCE3MPObdiKj7l27SI4CkkMZHewGML3ZVfULHy', 'user3@email.com', '2018-10-02 00:00:00', null, 'ROLE_USER');

insert into todo values (default, 1, 'Todo1_1', 'Description1', '2018-10-02 00:00:00', 'MEDIUM', 'IN_PROGRESS');
insert into todo values (default, 1, 'Todo1_2', 'Description2', '2018-10-02 00:00:00', 'LOW', 'IN_PROGRESS');
insert into todo values (default, 3, 'Todo3_1', 'Description1', '2018-10-02 00:00:00', 'HIGH', 'IN_PROGRESS') ;
