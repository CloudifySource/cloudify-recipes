# --- First database schema

# --- !Ups

create table company (
  id                        bigint not null auto_increment,
  name                      varchar(255) not null,
  constraint pk_company primary key (id))
;

create table computer (
  id                        bigint not null auto_increment,
  name                      varchar(255) not null,
  introduced                timestamp null,
  discontinued              timestamp null,
  company_id                bigint,
  constraint pk_computer primary key (id))
;

alter table computer add constraint fk_computer_company_1 foreign key (company_id) references company (id) on delete restrict on update restrict;
create index ix_computer_company_1 on computer (company_id);

# --- !Downs

drop table if exists computer;

drop table if exists company;



