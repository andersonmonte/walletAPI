create table users (
id serial,
name varchar(50),
password varchar,
email varchar(100),
primary key (id));

create table wallet(
id serial,
name varchar(60),
value numeric(10,2),
primary key (id));

