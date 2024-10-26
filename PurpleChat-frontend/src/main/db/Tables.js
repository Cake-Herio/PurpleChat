const add_tables = [

  `create table if not exists chat_session_user (
            user_id varchar not null default 0,
            contact_id varchar(11) not null,
            contact_type integer,
            session_id varchar,
            status integer default 1,
            contact_name varchar(20),
            last_message varchar(10000),
            last_receive_time bigint,
            no_read_count integer default 0,
            member_count integer,
            top_type integer default 0,
            primary key (user_id, contact_id)
        );`,
  `create table if not exists user_setting (
    user_id varchar not null,
    email varchar not null,
    sys_setting varchar,
    contact_no_read integer,
    server_port integer,
    token varchar,
    token_expiry datetime,
    nick_name varchar,
    primary key (user_id));
    
`,
  `create table if not exists chat_message (
            user_id varchar not null,
            message_id bigint not null default null,
            session_id varchar,
            message_type integer,
            message_content varchar,
            contact_type integer,
            send_user_id varchar,
            send_user_nick_name varchar,
            send_time bigint,
            status integer,
            file_size bigint,
            file_name varchar,
            file_path varchar,
            file_type integer,
            primary key (user_id, message_id)
        );`
]


const add_index = [
  `create index if not exists idx_session_id
        on chat_message (session_id);`,
]

const alter_tables = [
  // {
  //     tableName: 'chat_message',
  //     field: 'message_id',
  //     sql: 'alter table chat_message modify message_id bigint not null auto_increment;'
  // }


]

export {
  add_tables,
  add_index,
  alter_tables
}
