create table "user" (
    id bigserial primary key,
    username varchar (50) unique ,
    password varchar (60) not null, -- BCrypt length
    email varchar (50) not null unique,
    created_on timestamp not null,
    last_login_on timestamp,
    roles text
);

create table user_login (
    id bigserial primary key,
    auth_token varchar (300) not null unique,
    user_id bigint references "user",
    created_on timestamp not null,
    expires_on timestamp not null
);

create table todo (
    id bigserial primary key,
    user_id bigint references "user",
    title varchar (200) not null,
    description text,
    created_on timestamp not null,
    priority varchar (20) not null,
    status varchar(20) not null
);