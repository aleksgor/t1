------------------------------------------------------------------
--  TABLE test
------------------------------------------------------------------



CREATE TABLE test
(
   id              BIGINT,
   name            varchar (255),
   childid         BIGINT,
   secondChildId   BIGINT,
   blobField       blob,
   data            date,
   thirdChildId    BIGINT,
   money           DOUBLE,
   PRIMARY KEY (id)
);

------------------------------------------------------------------
--  TABLE child
------------------------------------------------------------------

CREATE TABLE child
(
   id     BIGINT,
   name   varchar (255),
   PRIMARY KEY (id)
);



CREATE TABLE db2test
(
   id              BIGINT,
   name            varchar (255),
   childid         BIGINT,
   secondChildId   BIGINT,
   blobField       blob,
   data            date,
   thirdChildId    BIGINT,
   money           DOUBLE,
   PRIMARY KEY (id)
);

------------------------------------------------------------------
--  TABLE child
------------------------------------------------------------------

CREATE TABLE db2child
(
   id     BIGINT,
   name   varchar (255),
   PRIMARY KEY (id)
);


------------------------------------------------------------------
--  TABLE sy_keys
------------------------------------------------------------------

CREATE TABLE sy_keys
(
   tablename   varchar (255),
   keyvalue    bigint,
   iterator    INTEGER DEFAULT 1,
   PRIMARY KEY (tablename)
);