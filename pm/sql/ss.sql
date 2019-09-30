//INSERT INTO test ( blob, secondChildId, childId, name, id, data ) VALUES (? ,? ,? ,? ,? ,? )

CREATE TABLE test (
        id INT4 NOT NULL,
        name TEXT,
        childid INT4,
        secondChildId INT4,
        blobField BLOB,
        data DATE
    );

CREATE UNIQUE INDEX test_pkey ON test (id ASC);

ALTER TABLE test ADD CONSTRAINT test_pkey PRIMARY KEY (id);


CREATE TABLE child (
        id INT4 NOT NULL,
        name TEXT
    );

CREATE UNIQUE INDEX child_pkey ON child (id ASC);

ALTER TABLE child ADD CONSTRAINT child_pkey PRIMARY KEY (id);
select * from test