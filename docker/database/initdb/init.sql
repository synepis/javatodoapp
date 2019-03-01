create user todo with password 'Password@123';
create user todo_flyway with password 'Password@123';

create database db_todo;

grant all privileges on database db_todo to todo_flyway;

