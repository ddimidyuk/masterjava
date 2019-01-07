DROP TABLE IF EXISTS cities CASCADE;
DROP SEQUENCE IF EXISTS city_seq;
DROP TABLE IF EXISTS users CASCADE;
DROP SEQUENCE IF EXISTS user_seq CASCADE;
DROP TYPE IF EXISTS user_flag CASCADE;
DROP TABLE IF EXISTS projects;
DROP SEQUENCE IF EXISTS project_seq;
DROP TABLE IF EXISTS groups;
DROP SEQUENCE IF EXISTS group_seq;
DROP TYPE IF EXISTS group_type;
DROP TABLE IF EXISTS user_groups;
DROP SEQUENCE IF EXISTS user_groups_seq;

CREATE SEQUENCE city_seq START 100000;

CREATE TABLE cities (
                      id        INTEGER PRIMARY KEY DEFAULT nextval('city_seq'),
                      code      TEXT UNIQUE NOT NULL,
                      name      TEXT NOT NULL
);

CREATE UNIQUE INDEX cities_id_idx ON cities (id);
CREATE UNIQUE INDEX cities_code_idx ON cities (code);


CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');

CREATE SEQUENCE user_seq START 100000;

CREATE TABLE users (
                     id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
                     full_name TEXT NOT NULL,
                     email     TEXT NOT NULL,
                     flag      user_flag NOT NULL,
                     city_id   INTEGER NOT NULL,
                     FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX email_idx ON users (email);


CREATE SEQUENCE project_seq START 100000;

CREATE TABLE projects (
                        id            INTEGER PRIMARY KEY DEFAULT nextval('project_seq'),
                        name          TEXT UNIQUE NOT NULL,
                        description   TEXT NOT NULL
);

CREATE UNIQUE INDEX projects_id_idx ON projects (id);


CREATE TYPE group_type AS ENUM ('REGISTERING', 'CURRENT', 'FINISHED');


CREATE SEQUENCE group_seq START 100000;

CREATE TABLE groups (
                      id            INTEGER PRIMARY KEY DEFAULT nextval('group_seq'),
                      name          TEXT NOT NULL,
                      type          group_type NOT NULL,
                      project_id    INTEGER NOT NULL,
                      FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX groups_id_idx ON groups (id);
CREATE UNIQUE INDEX groups_name_idx ON groups (name);


CREATE SEQUENCE user_groups_seq START 100000;

CREATE TABLE user_groups (
                           id            INTEGER PRIMARY KEY DEFAULT nextval('user_groups_seq'),
                           user_id       INTEGER NOT NULL,
                           group_id      INTEGER NOT NULL,
                           UNIQUE (user_id, group_id),
                           FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                           FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);

CREATE INDEX user_groups_user_id_idx ON user_groups (user_id);
